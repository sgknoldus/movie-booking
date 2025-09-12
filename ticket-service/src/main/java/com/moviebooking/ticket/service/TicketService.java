package com.moviebooking.ticket.service;

import com.moviebooking.ticket.domain.Ticket;
import com.moviebooking.ticket.domain.TicketStatus;
import com.moviebooking.ticket.dto.TicketResponse;
import com.moviebooking.booking.events.BookingConfirmedEvent;
import com.moviebooking.ticket.exception.TicketException;
import com.moviebooking.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;

    @Transactional
    public TicketResponse createTicketFromBooking(BookingConfirmedEvent event) {
        log.info("Creating ticket for booking: {}", event.getBookingId());

        // Check if ticket already exists for this booking
        if (ticketRepository.existsByBookingId(event.getBookingId())) {
            log.warn("Ticket already exists for booking: {}", event.getBookingId());
            throw new TicketException("Ticket already exists for booking: " + event.getBookingId());
        }

        String ticketId = generateTicketId();
        String qrCode = qrCodeService.generateQrCode(ticketId, event.getBookingId());

        Ticket ticket = Ticket.builder()
                .ticketId(ticketId)
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .showId(event.getShowId())
                .theatreId(event.getTheatreId())
                .movieId(event.getMovieId())
                .seatNumbers(event.getSeatNumbers())
                .totalAmount(event.getTotalAmount())
                .paymentId(event.getPaymentId())
                .showDateTime(event.getShowDateTime())
                .status(TicketStatus.ACTIVE)
                .qrCode(qrCode)
                .build();

        ticket = ticketRepository.save(ticket);
        log.info("Ticket created successfully: {}", ticket.getTicketId());

        return mapToTicketResponse(ticket);
    }

    public TicketResponse getTicketByTicketId(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketException("Ticket not found with ID: " + ticketId));
        
        return mapToTicketResponse(ticket);
    }

    public TicketResponse getTicketByBookingId(String bookingId) {
        Ticket ticket = ticketRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new TicketException("Ticket not found for booking: " + bookingId));
        
        return mapToTicketResponse(ticket);
    }

    public List<TicketResponse> getUserTickets(Long userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        return tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse cancelTicket(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketException("Ticket not found with ID: " + ticketId));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new TicketException("Cannot cancel ticket with status: " + ticket.getStatus());
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket = ticketRepository.save(ticket);

        log.info("Ticket cancelled successfully: {}", ticketId);
        return mapToTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse validateAndUseTicket(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketException("Ticket not found with ID: " + ticketId));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new TicketException("Ticket is not active. Current status: " + ticket.getStatus());
        }

        // Check if ticket is expired (show has passed)
        if (ticket.getShowDateTime().isBefore(java.time.LocalDateTime.now())) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            throw new TicketException("Ticket has expired. Show was scheduled for: " + ticket.getShowDateTime());
        }

        ticket.setStatus(TicketStatus.USED);
        ticket = ticketRepository.save(ticket);

        log.info("Ticket validated and marked as used: {}", ticketId);
        return mapToTicketResponse(ticket);
    }

    private String generateTicketId() {
        return "TK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .bookingId(ticket.getBookingId())
                .userId(ticket.getUserId())
                .showId(ticket.getShowId())
                .theatreId(ticket.getTheatreId())
                .movieId(ticket.getMovieId())
                .seatNumbers(ticket.getSeatNumbers())
                .totalAmount(ticket.getTotalAmount())
                .paymentId(ticket.getPaymentId())
                .showDateTime(ticket.getShowDateTime())
                .status(ticket.getStatus())
                .qrCode(ticket.getQrCode())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}