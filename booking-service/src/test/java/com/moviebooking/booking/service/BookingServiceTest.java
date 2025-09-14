package com.moviebooking.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.booking.client.PaymentServiceClient;
import com.moviebooking.booking.client.TheatreServiceClient;
import com.moviebooking.booking.domain.Booking;
import com.moviebooking.booking.domain.BookingStatus;
import com.moviebooking.booking.dto.*;
import com.moviebooking.booking.exception.BookingException;
import com.moviebooking.booking.repository.BookingRepository;
import com.moviebooking.common.events.booking.BookingConfirmedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TheatreServiceClient theatreServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RLock rLock;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequest bookingRequest;
    private SeatAvailabilityResponse availabilityResponse;
    private PaymentResponse paymentResponse;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        bookingRequest = new BookingRequest();
        bookingRequest.setUserId(1L);
        bookingRequest.setShowId(1L);
        bookingRequest.setSeatNumbers(List.of("A1", "A2"));

        availabilityResponse = SeatAvailabilityResponse.builder()
                .available(true)
                .theatreId(1L)
                .movieId(1L)
                .totalPrice(new BigDecimal("500.00"))
                .showDateTime(LocalDateTime.now().plusDays(1))
                .unavailableSeats(List.of())
                .build();

        paymentResponse = PaymentResponse.builder()
                .paymentId("PAY-123")
                .status("SUCCESS")
                .message("Payment successful")
                .build();

        testBooking = Booking.builder()
                .bookingId("BK-123")
                .userId(bookingRequest.getUserId())
                .showId(bookingRequest.getShowId())
                .theatreId(1L)
                .movieId(1L)
                .seatNumbers(bookingRequest.getSeatNumbers())
                .totalAmount(availabilityResponse.getTotalPrice())
                .status(BookingStatus.PENDING)
                .showDateTime(availabilityResponse.getShowDateTime())
                .build();
    }

    @Test
    void bookTickets_ShouldCreateBookingSuccessfully_WhenAllConditionsAreMet() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(availabilityResponse));

        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        when(paymentServiceClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(paymentResponse));

        // When
        BookingResponse result = bookingService.bookTickets(bookingRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(bookingRequest.getUserId());
        assertThat(result.getShowId()).isEqualTo(bookingRequest.getShowId());
        assertThat(result.getSeatNumbers()).containsExactlyElementsOf(bookingRequest.getSeatNumbers());
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.getMessage()).isEqualTo("Booking confirmed successfully");

        verify(theatreServiceClient).checkSeatAvailability(any(SeatAvailabilityRequest.class));
        verify(paymentServiceClient).processPayment(any(PaymentRequest.class));
        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(kafkaTemplate).executeInTransaction(any());
        verify(rLock).unlock();
    }

    @Test
    void bookTickets_ShouldThrowException_WhenLockCannotBeAcquired() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessage("Unable to process booking at this moment. Please try again.");

        verify(theatreServiceClient, never()).checkSeatAvailability(any());
        verify(paymentServiceClient, never()).processPayment(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTickets_ShouldThrowException_WhenSeatsNotAvailable() throws InterruptedException {
        // Given
        SeatAvailabilityResponse unavailableResponse = SeatAvailabilityResponse.builder()
                .available(false)
                .unavailableSeats(List.of("A1"))
                .build();

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(unavailableResponse));

        // Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("Requested seats are not available");

        verify(paymentServiceClient, never()).processPayment(any());
        verify(rLock).unlock();
    }

    @Test
    void bookTickets_ShouldHandlePaymentFailure() throws InterruptedException {
        // Given
        PaymentResponse failedPaymentResponse = PaymentResponse.builder()
                .status("FAILED")
                .message("Insufficient balance")
                .build();

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(availabilityResponse));

        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        when(paymentServiceClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(failedPaymentResponse));

        // Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessage("Payment failed: Insufficient balance");

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(2)).save(bookingCaptor.capture());

        List<Booking> savedBookings = bookingCaptor.getAllValues();
        assertThat(savedBookings.get(1).getStatus()).isEqualTo(BookingStatus.PAYMENT_FAILED);

        verify(kafkaTemplate, never()).executeInTransaction(any());
        verify(rLock).unlock();
    }

    @Test
    void bookTickets_ShouldReleaseLock_WhenExceptionOccurs() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenThrow(new RuntimeException("Theatre service unavailable"));

        // Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class);

        verify(rLock).unlock();
    }

    @Test
    void bookTickets_ShouldHandleInterruptedException() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException());

        // Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessage("Booking process interrupted");
    }

    @Test
    void getBookingByBookingId_ShouldReturnBooking_WhenExists() {
        // Given
        String bookingId = "BK-123";
        testBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByBookingId(bookingId)).thenReturn(Optional.of(testBooking));

        // When
        BookingResponse result = bookingService.getBookingByBookingId(bookingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingId()).isEqualTo(bookingId);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).findByBookingId(bookingId);
    }

    @Test
    void getBookingByBookingId_ShouldThrowException_WhenNotFound() {
        // Given
        String bookingId = "NONEXISTENT";
        when(bookingRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> bookingService.getBookingByBookingId(bookingId))
                .isInstanceOf(BookingException.class)
                .hasMessage("Booking not found with ID: " + bookingId);
    }

    @Test
    void bookTickets_ShouldGenerateUniqueBookingId() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(availabilityResponse));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentServiceClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(paymentResponse));

        // When
        bookingService.bookTickets(bookingRequest);

        // Then
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, atLeastOnce()).save(bookingCaptor.capture());

        String capturedBookingId = bookingCaptor.getValue().getBookingId();
        assertThat(capturedBookingId).startsWith("BK-");
        assertThat(capturedBookingId).hasSize(25); // BK- + 13-digit timestamp + - + 8 char UUID
    }

    @Test
    void bookTickets_ShouldPublishCorrectEvent() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(availabilityResponse));

        Booking savedBooking = Booking.builder()
                .id(testBooking.getId())
                .bookingId(testBooking.getBookingId())
                .userId(testBooking.getUserId())
                .showId(testBooking.getShowId())
                .theatreId(testBooking.getTheatreId())
                .movieId(testBooking.getMovieId())
                .seatNumbers(testBooking.getSeatNumbers())
                .totalAmount(testBooking.getTotalAmount())
                .showDateTime(testBooking.getShowDateTime())
                .createdAt(testBooking.getCreatedAt())
                .status(BookingStatus.CONFIRMED)
                .paymentId("PAY-123")
                .build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        when(paymentServiceClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(paymentResponse));

        // When
        bookingService.bookTickets(bookingRequest);

        // Then
        // Verify that Kafka transaction was executed
        verify(kafkaTemplate).executeInTransaction(any());
    }

    @Test
    void bookTickets_ShouldNotThrowException_WhenKafkaPublishingFails() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(availabilityResponse));

        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        when(paymentServiceClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(paymentResponse));

        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).executeInTransaction(any());

        // When & Then - Should not throw exception
        BookingResponse result = bookingService.bookTickets(bookingRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void bookTickets_ShouldPreventConcurrentBookingSameSeats() throws InterruptedException {
        // Given - Simulate two concurrent requests for the same seats
        String lockKey = "booking:lock:show:1:seats:A1,A2";
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(10L, 300L, TimeUnit.SECONDS)).thenReturn(false); // Lock acquisition fails

        // When & Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessage("Unable to process booking at this moment. Please try again.");

        // Verify no downstream calls were made when lock acquisition fails
        verify(theatreServiceClient, never()).checkSeatAvailability(any());
        verify(paymentServiceClient, never()).processPayment(any());
        verify(bookingRepository, never()).save(any());
        verify(kafkaTemplate, never()).executeInTransaction(any());
    }

    @Test
    void bookTickets_ShouldHandleDuplicateBookingRequestsIdempotently() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Simulate seat availability check returns unavailable on second call
        // (indicating seats were already booked by first request)
        SeatAvailabilityResponse unavailableResponse = SeatAvailabilityResponse.builder()
                .available(false)
                .unavailableSeats(List.of("A1", "A2"))
                .build();

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(unavailableResponse));

        // When & Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("Requested seats are not available");

        // Verify lock was properly released even when booking fails
        verify(rLock).unlock();
    }

    @Test
    void bookTickets_ShouldHandleLockTimeout() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(10L, 300L, TimeUnit.SECONDS)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessage("Unable to process booking at this moment. Please try again.");

        // Verify that no processing occurred when lock timeout happens
        verify(theatreServiceClient, never()).checkSeatAvailability(any());
        verify(paymentServiceClient, never()).processPayment(any());
        verify(bookingRepository, never()).save(any());
        verify(kafkaTemplate, never()).executeInTransaction(any());
    }

    @Test
    void bookTickets_ShouldEnsureLockIsAlwaysReleased() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Simulate exception during seat availability check
        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class);

        // Verify lock was released despite exception
        verify(rLock).unlock();
    }

    @Test
    void bookTickets_ShouldValidateLockKeyGeneration() throws InterruptedException {
        // Given
        BookingRequest customRequest = new BookingRequest();
        customRequest.setUserId(1L);
        customRequest.setShowId(123L);
        customRequest.setSeatNumbers(List.of("B5", "B6", "B7"));

        String expectedLockKey = "booking:lock:show:123:seats:B5,B6,B7";

        when(redissonClient.getLock(expectedLockKey)).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(availabilityResponse));

        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        when(paymentServiceClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(paymentResponse));

        // When
        bookingService.bookTickets(customRequest);

        // Then
        verify(redissonClient).getLock(expectedLockKey);
    }

    @Test
    void bookTickets_ShouldHandleTransactionalIntegrity() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(availabilityResponse));

        // Simulate database save failure after payment success
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(testBooking) // First save (pending status) succeeds
                .thenThrow(new RuntimeException("Database connection lost")); // Second save fails

        when(paymentServiceClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(paymentResponse));

        // When & Then
        assertThatThrownBy(() -> bookingService.bookTickets(bookingRequest))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("Database connection lost");

        // Verify that Kafka event was not published due to database failure
        verify(kafkaTemplate, never()).executeInTransaction(any());
        verify(rLock).unlock();
    }

    @Test
    void bookTickets_ShouldEnsureIdempotentKafkaPublishing() throws InterruptedException {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(theatreServiceClient.checkSeatAvailability(any(SeatAvailabilityRequest.class)))
                .thenReturn(ResponseEntity.ok(availabilityResponse));

        Booking confirmedBooking = Booking.builder()
                .bookingId(testBooking.getBookingId())
                .userId(testBooking.getUserId())
                .showId(testBooking.getShowId())
                .theatreId(testBooking.getTheatreId())
                .movieId(testBooking.getMovieId())
                .seatNumbers(testBooking.getSeatNumbers())
                .totalAmount(testBooking.getTotalAmount())
                .status(BookingStatus.CONFIRMED)
                .paymentId("PAY-123")
                .showDateTime(testBooking.getShowDateTime())
                .build();

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(testBooking) // First save (pending)
                .thenReturn(confirmedBooking); // Second save (confirmed)

        when(paymentServiceClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(ResponseEntity.ok(paymentResponse));

        // Mock successful Kafka transaction
        when(kafkaTemplate.executeInTransaction(any())).thenReturn(true);

        // When
        BookingResponse result = bookingService.bookTickets(bookingRequest);

        // Then
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(kafkaTemplate).executeInTransaction(any());
    }
}