package com.moviebooking.ticket.service;

import com.moviebooking.common.events.booking.BookingConfirmedEvent;
import com.moviebooking.ticket.domain.Ticket;
import com.moviebooking.ticket.domain.TicketStatus;
import com.moviebooking.ticket.dto.TicketResponse;
import com.moviebooking.ticket.exception.TicketException;
import com.moviebooking.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private TicketService ticketService;

    private BookingConfirmedEvent bookingEvent;
    private Ticket testTicket;
    private String testTicketId;
    private String testBookingId;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        testTicketId = "TK-123456789-abcd1234";
        testBookingId = "BK-123456789-efgh5678";
        testUserId = 1L;

        bookingEvent = BookingConfirmedEvent.builder()
                .bookingId(testBookingId)
                .userId(testUserId)
                .showId(1L)
                .theatreId(1L)
                .movieId(1L)
                .seatNumbers(List.of("A1", "A2"))
                .totalAmount(BigDecimal.valueOf(500.00))
                .paymentId("PAY-123")
                .showDateTime(LocalDateTime.now().plusDays(1))
                .confirmedAt(LocalDateTime.now())
                .build();

        testTicket = Ticket.builder()
                .ticketId(testTicketId)
                .bookingId(testBookingId)
                .userId(testUserId)
                .showId(1L)
                .theatreId(1L)
                .movieId(1L)
                .seatNumbers(List.of("A1", "A2"))
                .totalAmount(BigDecimal.valueOf(500.00))
                .paymentId("PAY-123")
                .showDateTime(LocalDateTime.now().plusDays(1))
                .status(TicketStatus.ACTIVE)
                .qrCode("QR-CODE-123")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTicketFromBooking_ShouldCreateTicketSuccessfully() {
        // Given
        when(ticketRepository.existsByBookingId(testBookingId)).thenReturn(false);
        when(qrCodeService.generateQrCode(anyString(), eq(testBookingId))).thenReturn("QR-CODE-123");
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        TicketResponse result = ticketService.createTicketFromBooking(bookingEvent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingId()).isEqualTo(testBookingId);
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getStatus()).isEqualTo(TicketStatus.ACTIVE);
        assertThat(result.getSeatNumbers()).containsExactlyElementsOf(List.of("A1", "A2"));
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(result.getQrCode()).isEqualTo("QR-CODE-123");

        verify(ticketRepository).existsByBookingId(testBookingId);
        verify(qrCodeService).generateQrCode(anyString(), eq(testBookingId));
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicketFromBooking_ShouldThrowException_WhenTicketAlreadyExists() {
        // Given
        when(ticketRepository.existsByBookingId(testBookingId)).thenReturn(true);

        // Then
        assertThatThrownBy(() -> ticketService.createTicketFromBooking(bookingEvent))
                .isInstanceOf(TicketException.class)
                .hasMessage("Ticket already exists for booking: " + testBookingId);

        verify(qrCodeService, never()).generateQrCode(anyString(), anyString());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void createTicketFromBooking_ShouldGenerateUniqueTicketId() {
        // Given
        when(ticketRepository.existsByBookingId(testBookingId)).thenReturn(false);
        when(qrCodeService.generateQrCode(anyString(), eq(testBookingId))).thenReturn("QR-CODE-123");
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        ticketService.createTicketFromBooking(bookingEvent);

        // Then
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());

        Ticket savedTicket = ticketCaptor.getValue();
        assertThat(savedTicket.getTicketId()).startsWith("TK-");
        assertThat(savedTicket.getTicketId()).hasSize(25); // TK- + 13-digit timestamp + - + 8 char UUID
    }

    @Test
    void getTicketByTicketId_ShouldReturnTicket_WhenExists() {
        // Given
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.of(testTicket));

        // When
        TicketResponse result = ticketService.getTicketByTicketId(testTicketId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTicketId()).isEqualTo(testTicketId);
        assertThat(result.getStatus()).isEqualTo(TicketStatus.ACTIVE);
        verify(ticketRepository).findByTicketId(testTicketId);
    }

    @Test
    void getTicketByTicketId_ShouldThrowException_WhenNotFound() {
        // Given
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> ticketService.getTicketByTicketId(testTicketId))
                .isInstanceOf(TicketException.class)
                .hasMessage("Ticket not found with ID: " + testTicketId);
    }

    @Test
    void getTicketByBookingId_ShouldReturnTicket_WhenExists() {
        // Given
        when(ticketRepository.findByBookingId(testBookingId)).thenReturn(Optional.of(testTicket));

        // When
        TicketResponse result = ticketService.getTicketByBookingId(testBookingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingId()).isEqualTo(testBookingId);
        verify(ticketRepository).findByBookingId(testBookingId);
    }

    @Test
    void getTicketByBookingId_ShouldThrowException_WhenNotFound() {
        // Given
        when(ticketRepository.findByBookingId(testBookingId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> ticketService.getTicketByBookingId(testBookingId))
                .isInstanceOf(TicketException.class)
                .hasMessage("Ticket not found for booking: " + testBookingId);
    }

    @Test
    void getUserTickets_ShouldReturnUserTickets() {
        // Given
        Ticket ticket2 = Ticket.builder()
                .id(2L)
                .ticketId("TK-987654321-wxyz9876")
                .bookingId("BK-987654321-mnop5432")
                .userId(testUserId)
                .showId(testTicket.getShowId())
                .theatreId(testTicket.getTheatreId())
                .movieId(testTicket.getMovieId())
                .seatNumbers(testTicket.getSeatNumbers())
                .totalAmount(testTicket.getTotalAmount())
                .paymentId(testTicket.getPaymentId())
                .showDateTime(testTicket.getShowDateTime())
                .status(testTicket.getStatus())
                .qrCode(testTicket.getQrCode())
                .createdAt(testTicket.getCreatedAt())
                .build();

        when(ticketRepository.findByUserId(testUserId)).thenReturn(List.of(testTicket, ticket2));

        // When
        List<TicketResponse> result = ticketService.getUserTickets(testUserId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(testUserId);
        assertThat(result.get(1).getUserId()).isEqualTo(testUserId);
        verify(ticketRepository).findByUserId(testUserId);
    }

    @Test
    void getUserTickets_ShouldReturnEmptyList_WhenNoTickets() {
        // Given
        when(ticketRepository.findByUserId(testUserId)).thenReturn(List.of());

        // When
        List<TicketResponse> result = ticketService.getUserTickets(testUserId);

        // Then
        assertThat(result).isEmpty();
        verify(ticketRepository).findByUserId(testUserId);
    }

    @Test
    void cancelTicket_ShouldCancelActiveTicket() {
        // Given
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        TicketResponse result = ticketService.cancelTicket(testTicketId);

        // Then
        assertThat(result).isNotNull();
        assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository).save(testTicket);
    }

    @Test
    void cancelTicket_ShouldThrowException_WhenTicketNotFound() {
        // Given
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> ticketService.cancelTicket(testTicketId))
                .isInstanceOf(TicketException.class)
                .hasMessage("Ticket not found with ID: " + testTicketId);

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancelTicket_ShouldThrowException_WhenTicketNotActive() {
        // Given
        testTicket.setStatus(TicketStatus.USED);
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.of(testTicket));

        // Then
        assertThatThrownBy(() -> ticketService.cancelTicket(testTicketId))
                .isInstanceOf(TicketException.class)
                .hasMessage("Cannot cancel ticket with status: USED");

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void validateAndUseTicket_ShouldUseActiveTicket() {
        // Given
        testTicket.setShowDateTime(LocalDateTime.now().plusHours(2)); // Future show
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        TicketResponse result = ticketService.validateAndUseTicket(testTicketId);

        // Then
        assertThat(result).isNotNull();
        assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository).save(testTicket);
    }

    @Test
    void validateAndUseTicket_ShouldThrowException_WhenTicketNotFound() {
        // Given
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> ticketService.validateAndUseTicket(testTicketId))
                .isInstanceOf(TicketException.class)
                .hasMessage("Ticket not found with ID: " + testTicketId);
    }

    @Test
    void validateAndUseTicket_ShouldThrowException_WhenTicketNotActive() {
        // Given
        testTicket.setStatus(TicketStatus.CANCELLED);
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.of(testTicket));

        // Then
        assertThatThrownBy(() -> ticketService.validateAndUseTicket(testTicketId))
                .isInstanceOf(TicketException.class)
                .hasMessage("Ticket is not active. Current status: CANCELLED");

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void validateAndUseTicket_ShouldMarkAsExpired_WhenShowHasPassed() {
        // Given
        testTicket.setShowDateTime(LocalDateTime.now().minusHours(1)); // Past show
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // Then
        assertThatThrownBy(() -> ticketService.validateAndUseTicket(testTicketId))
                .isInstanceOf(TicketException.class)
                .hasMessageContaining("Ticket has expired");

        assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.EXPIRED);
        verify(ticketRepository).save(testTicket);
    }

    @Test
    void mapToTicketResponse_ShouldMapAllFields() {
        // Given
        when(ticketRepository.findByTicketId(testTicketId)).thenReturn(Optional.of(testTicket));

        // When
        TicketResponse result = ticketService.getTicketByTicketId(testTicketId);

        // Then
        assertThat(result.getTicketId()).isEqualTo(testTicket.getTicketId());
        assertThat(result.getBookingId()).isEqualTo(testTicket.getBookingId());
        assertThat(result.getUserId()).isEqualTo(testTicket.getUserId());
        assertThat(result.getShowId()).isEqualTo(testTicket.getShowId());
        assertThat(result.getTheatreId()).isEqualTo(testTicket.getTheatreId());
        assertThat(result.getMovieId()).isEqualTo(testTicket.getMovieId());
        assertThat(result.getSeatNumbers()).isEqualTo(testTicket.getSeatNumbers());
        assertThat(result.getTotalAmount()).isEqualTo(testTicket.getTotalAmount());
        assertThat(result.getPaymentId()).isEqualTo(testTicket.getPaymentId());
        assertThat(result.getShowDateTime()).isEqualTo(testTicket.getShowDateTime());
        assertThat(result.getStatus()).isEqualTo(testTicket.getStatus());
        assertThat(result.getQrCode()).isEqualTo(testTicket.getQrCode());
        assertThat(result.getCreatedAt()).isEqualTo(testTicket.getCreatedAt());
    }

    @Test
    void createTicketFromBooking_ShouldSetAllFieldsFromEvent() {
        // Given
        when(ticketRepository.existsByBookingId(testBookingId)).thenReturn(false);
        when(qrCodeService.generateQrCode(anyString(), eq(testBookingId))).thenReturn("QR-CODE-123");
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        ticketService.createTicketFromBooking(bookingEvent);

        // Then
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());

        Ticket savedTicket = ticketCaptor.getValue();
        assertThat(savedTicket.getBookingId()).isEqualTo(bookingEvent.getBookingId());
        assertThat(savedTicket.getUserId()).isEqualTo(bookingEvent.getUserId());
        assertThat(savedTicket.getShowId()).isEqualTo(bookingEvent.getShowId());
        assertThat(savedTicket.getTheatreId()).isEqualTo(bookingEvent.getTheatreId());
        assertThat(savedTicket.getMovieId()).isEqualTo(bookingEvent.getMovieId());
        assertThat(savedTicket.getSeatNumbers()).isEqualTo(bookingEvent.getSeatNumbers());
        assertThat(savedTicket.getTotalAmount()).isEqualTo(bookingEvent.getTotalAmount());
        assertThat(savedTicket.getPaymentId()).isEqualTo(bookingEvent.getPaymentId());
        assertThat(savedTicket.getShowDateTime()).isEqualTo(bookingEvent.getShowDateTime());
        assertThat(savedTicket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
        assertThat(savedTicket.getQrCode()).isEqualTo("QR-CODE-123");
    }
}