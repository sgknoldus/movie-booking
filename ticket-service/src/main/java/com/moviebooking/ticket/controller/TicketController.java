package com.moviebooking.ticket.controller;

import com.moviebooking.ticket.dto.TicketResponse;
import com.moviebooking.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ticket", description = "Movie ticket management operations")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket details", description = "Retrieve ticket details by ticket ID")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable String ticketId) {
        log.info("Retrieving ticket details for ticket ID: {}", ticketId);
        
        TicketResponse response = ticketService.getTicketByTicketId(ticketId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get ticket by booking ID", description = "Retrieve ticket details by booking ID")
    public ResponseEntity<TicketResponse> getTicketByBookingId(@PathVariable String bookingId) {
        log.info("Retrieving ticket details for booking ID: {}", bookingId);
        
        TicketResponse response = ticketService.getTicketByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user tickets", description = "Retrieve all tickets for a specific user")
    public ResponseEntity<List<TicketResponse>> getUserTickets(@PathVariable Long userId) {
        log.info("Retrieving tickets for user: {}", userId);
        
        List<TicketResponse> tickets = ticketService.getUserTickets(userId);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{ticketId}/cancel")
    @Operation(summary = "Cancel ticket", description = "Cancel an active ticket")
    public ResponseEntity<TicketResponse> cancelTicket(@PathVariable String ticketId) {
        log.info("Cancelling ticket: {}", ticketId);
        
        TicketResponse response = ticketService.cancelTicket(ticketId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}/validate")
    @Operation(summary = "Validate and use ticket", description = "Validate ticket for entry and mark as used")
    public ResponseEntity<TicketResponse> validateTicket(@PathVariable String ticketId) {
        log.info("Validating ticket: {}", ticketId);
        
        TicketResponse response = ticketService.validateAndUseTicket(ticketId);
        return ResponseEntity.ok(response);
    }
}