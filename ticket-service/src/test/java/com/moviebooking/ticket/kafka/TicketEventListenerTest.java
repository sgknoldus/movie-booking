package com.moviebooking.ticket.kafka;

import com.moviebooking.common.events.booking.BookingConfirmedEvent;
import com.moviebooking.ticket.dto.TicketResponse;
import com.moviebooking.ticket.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketEventListenerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketEventListener ticketEventListener;

    private BookingConfirmedEvent bookingEvent;
    private TicketResponse ticketResponse;

    @BeforeEach
    void setUp() {
        bookingEvent = BookingConfirmedEvent.builder()
                .bookingId("BK-123456789-efgh5678")
                .userId(1L)
                .showId(1L)
                .theatreId(1L)
                .movieId(1L)
                .seatNumbers(List.of("A1", "A2"))
                .totalAmount(BigDecimal.valueOf(500.00))
                .paymentId("PAY-123")
                .showDateTime(LocalDateTime.now().plusDays(1))
                .confirmedAt(LocalDateTime.now())
                .build();

        ticketResponse = TicketResponse.builder()
                .ticketId("TK-123456789-abcd1234")
                .bookingId(bookingEvent.getBookingId())
                .userId(bookingEvent.getUserId())
                .showId(bookingEvent.getShowId())
                .theatreId(bookingEvent.getTheatreId())
                .movieId(bookingEvent.getMovieId())
                .seatNumbers(bookingEvent.getSeatNumbers())
                .totalAmount(bookingEvent.getTotalAmount())
                .paymentId(bookingEvent.getPaymentId())
                .showDateTime(bookingEvent.getShowDateTime())
                .qrCode("QR-CODE-123")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void handleBookingConfirmed_ShouldCreateTicketSuccessfully() {
        // Given
        when(ticketService.createTicketFromBooking(bookingEvent)).thenReturn(ticketResponse);

        // When
        ticketEventListener.handleBookingConfirmed(bookingEvent);

        // Then
        verify(ticketService).createTicketFromBooking(bookingEvent);
    }

    @Test
    void handleBookingConfirmed_ShouldHandleServiceException() {
        // Given
        when(ticketService.createTicketFromBooking(bookingEvent))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then - Should not throw exception, should log error instead
        ticketEventListener.handleBookingConfirmed(bookingEvent);

        verify(ticketService).createTicketFromBooking(bookingEvent);
    }

    @Test
    void handleBookingConfirmed_ShouldHandleNullEvent() {
        // When & Then - Should not throw exception
        ticketEventListener.handleBookingConfirmed(null);

        // Depending on implementation, service might be called or not
        // But listener should handle gracefully
    }

    @Test
    void handleBookingConfirmed_ShouldHandleMultipleEvents() {
        // Given
        BookingConfirmedEvent event1 = bookingEvent;
        BookingConfirmedEvent event2 = BookingConfirmedEvent.builder()
                .bookingId("BK-987654321-mnop5432")
                .userId(2L)
                .showId(2L)
                .theatreId(2L)
                .movieId(2L)
                .seatNumbers(List.of("B1", "B2", "B3"))
                .totalAmount(BigDecimal.valueOf(750.00))
                .paymentId("PAY-456")
                .showDateTime(LocalDateTime.now().plusDays(2))
                .confirmedAt(LocalDateTime.now())
                .build();

        TicketResponse response2 = TicketResponse.builder()
                .ticketId("TK-987654321-wxyz9876")
                .bookingId(event2.getBookingId())
                .userId(event2.getUserId())
                .build();

        when(ticketService.createTicketFromBooking(event1)).thenReturn(ticketResponse);
        when(ticketService.createTicketFromBooking(event2)).thenReturn(response2);

        // When
        ticketEventListener.handleBookingConfirmed(event1);
        ticketEventListener.handleBookingConfirmed(event2);

        // Then
        verify(ticketService).createTicketFromBooking(event1);
        verify(ticketService).createTicketFromBooking(event2);
    }

    @Test
    void handleBookingConfirmed_ShouldHandlePartialFailures() {
        // Given
        BookingConfirmedEvent successEvent = bookingEvent;
        BookingConfirmedEvent failEvent = BookingConfirmedEvent.builder()
                .bookingId("BK-FAIL-123")
                .userId(999L)
                .showId(999L)
                .theatreId(999L)
                .movieId(999L)
                .seatNumbers(List.of("Z99"))
                .totalAmount(BigDecimal.valueOf(100.00))
                .paymentId("PAY-FAIL")
                .showDateTime(LocalDateTime.now().plusDays(1))
                .confirmedAt(LocalDateTime.now())
                .build();

        when(ticketService.createTicketFromBooking(successEvent)).thenReturn(ticketResponse);
        when(ticketService.createTicketFromBooking(failEvent))
                .thenThrow(new RuntimeException("Ticket creation failed"));

        // When
        ticketEventListener.handleBookingConfirmed(successEvent);
        ticketEventListener.handleBookingConfirmed(failEvent);

        // Then
        verify(ticketService).createTicketFromBooking(successEvent);
        verify(ticketService).createTicketFromBooking(failEvent);
    }

    @Test
    void handleBookingConfirmed_ShouldHandleEventWithMinimalData() {
        // Given
        BookingConfirmedEvent minimalEvent = BookingConfirmedEvent.builder()
                .bookingId("BK-MINIMAL")
                .userId(1L)
                .showId(1L)
                .theatreId(1L)
                .movieId(1L)
                .seatNumbers(List.of("A1"))
                .totalAmount(BigDecimal.valueOf(250.00))
                .paymentId("PAY-MIN")
                .showDateTime(LocalDateTime.now().plusHours(2))
                .confirmedAt(LocalDateTime.now())
                .build();

        TicketResponse minimalResponse = TicketResponse.builder()
                .ticketId("TK-MINIMAL")
                .bookingId(minimalEvent.getBookingId())
                .build();

        when(ticketService.createTicketFromBooking(minimalEvent)).thenReturn(minimalResponse);

        // When
        ticketEventListener.handleBookingConfirmed(minimalEvent);

        // Then
        verify(ticketService).createTicketFromBooking(minimalEvent);
    }

    @Test
    void handleBookingConfirmed_ShouldHandleEventWithLargeNumberOfSeats() {
        // Given
        List<String> manySeats = List.of(
                "A1", "A2", "A3", "A4", "A5",
                "B1", "B2", "B3", "B4", "B5",
                "C1", "C2", "C3", "C4", "C5"
        );

        BookingConfirmedEvent largeSeatEvent = BookingConfirmedEvent.builder()
                .bookingId("BK-LARGE-GROUP")
                .userId(1L)
                .showId(1L)
                .theatreId(1L)
                .movieId(1L)
                .seatNumbers(manySeats)
                .totalAmount(BigDecimal.valueOf(3750.00)) // 15 seats * 250
                .paymentId("PAY-GROUP")
                .showDateTime(LocalDateTime.now().plusDays(1))
                .confirmedAt(LocalDateTime.now())
                .build();

        TicketResponse groupResponse = TicketResponse.builder()
                .ticketId("TK-GROUP-123")
                .bookingId(largeSeatEvent.getBookingId())
                .seatNumbers(manySeats)
                .totalAmount(largeSeatEvent.getTotalAmount())
                .build();

        when(ticketService.createTicketFromBooking(largeSeatEvent)).thenReturn(groupResponse);

        // When
        ticketEventListener.handleBookingConfirmed(largeSeatEvent);

        // Then
        verify(ticketService).createTicketFromBooking(largeSeatEvent);
    }

    @Test
    void handleBookingConfirmed_ShouldNotRetryOnException() {
        // Given
        when(ticketService.createTicketFromBooking(bookingEvent))
                .thenThrow(new RuntimeException("Transient failure"));

        // When
        ticketEventListener.handleBookingConfirmed(bookingEvent);

        // Then
        verify(ticketService, times(1)).createTicketFromBooking(bookingEvent);
        // Verify it doesn't retry automatically (implementation doesn't include retry logic)
    }

    @Test
    void handleBookingConfirmed_ShouldHandleServiceTimeout() {
        // Given
        when(ticketService.createTicketFromBooking(bookingEvent))
                .thenThrow(new RuntimeException("Request timeout"));

        // When & Then - Should handle gracefully without throwing
        ticketEventListener.handleBookingConfirmed(bookingEvent);

        verify(ticketService).createTicketFromBooking(bookingEvent);
    }
}