package com.moviebooking.ticket.controller;

import com.moviebooking.ticket.dto.TicketResponse;
import com.moviebooking.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Ticket Management",
    description = "Comprehensive ticket lifecycle management including retrieval, status updates, validation, and cancellation. Handles e-tickets and physical ticket operations."
)
@SecurityRequirement(name = "Bearer Authentication")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket details", description = "Retrieve ticket details by ticket ID")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable String ticketId) {
        log.info("Retrieving ticket details for ticket ID: {}", ticketId);
        
        TicketResponse response = ticketService.getTicketByTicketId(ticketId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "Retrieve tickets with filters",
        description = "Fetches ticket information based on filter criteria. Supports filtering by booking ID for specific tickets or user ID for all user tickets. At least one filter parameter is required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tickets retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Single Ticket (by bookingId)",
                        value = "{\"ticketId\":\"TKT789123456\",\"bookingId\":\"BK123456789\",\"userId\":1,\"movieTitle\":\"Avengers: Endgame\",\"theatreName\":\"PVR Cinemas\",\"seatNumber\":\"A1\",\"showDateTime\":\"2024-01-15T19:30:00Z\",\"status\":\"ACTIVE\",\"qrCode\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEU...\",\"price\":250.00}"
                    ),
                    @ExampleObject(
                        name = "User Tickets List (by userId)",
                        value = "[{\"ticketId\":\"TKT789123456\",\"movieTitle\":\"Avengers: Endgame\",\"status\":\"ACTIVE\"},{\"ticketId\":\"TKT789123457\",\"movieTitle\":\"Spider-Man\",\"status\":\"USED\"}]"
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Missing required filter parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot access another user's tickets"),
        @ApiResponse(responseCode = "404", description = "No tickets found matching the criteria")
    })
    public ResponseEntity<?> getTickets(
            @Parameter(
                description = "Filter by booking ID to get specific ticket",
                example = "BK123456789"
            )
            @RequestParam(required = false) String bookingId,
            @Parameter(
                description = "Filter by user ID to get all user tickets",
                example = "1"
            )
            @RequestParam(required = false) Long userId) {

        if (bookingId != null) {
            log.info("Retrieving ticket details for booking ID: {}", bookingId);
            TicketResponse response = ticketService.getTicketByBookingId(bookingId);
            return ResponseEntity.ok(response);
        }

        if (userId != null) {
            log.info("Retrieving tickets for user: {}", userId);
            List<TicketResponse> tickets = ticketService.getUserTickets(userId);
            return ResponseEntity.ok(tickets);
        }

        return ResponseEntity.badRequest().body("Either bookingId or userId parameter is required");
    }

    @PatchMapping("/{ticketId}")
    @Operation(
        summary = "Update ticket status",
        description = "Performs status updates on tickets including cancellation, validation for entry, and marking as used. Each action transitions the ticket through its lifecycle states."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ticket status updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TicketResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Cancelled Ticket",
                        value = "{\"ticketId\":\"TKT789123456\",\"status\":\"CANCELLED\",\"updatedAt\":\"2024-01-15T16:00:00Z\",\"refundAmount\":200.00}"
                    ),
                    @ExampleObject(
                        name = "Validated Ticket",
                        value = "{\"ticketId\":\"TKT789123456\",\"status\":\"USED\",\"validatedAt\":\"2024-01-15T19:25:00Z\",\"validatedBy\":\"STAFF123\"}"
                    )
                }
            )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid action or ticket state"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions for this action"),
        @ApiResponse(responseCode = "404", description = "Ticket not found with the provided ID"),
        @ApiResponse(responseCode = "409", description = "Conflict - Ticket cannot be updated in current state")
    })
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @Parameter(
                description = "Unique ticket identifier",
                example = "TKT789123456",
                required = true
            )
            @PathVariable String ticketId,
            @Parameter(
                description = "Action to perform on the ticket",
                example = "cancel",
                required = true,
                schema = @Schema(allowableValues = {"cancel", "validate"})
            )
            @RequestParam String action) {

        switch (action.toLowerCase()) {
            case "cancel":
                log.info("Cancelling ticket: {}", ticketId);
                TicketResponse cancelResponse = ticketService.cancelTicket(ticketId);
                return ResponseEntity.ok(cancelResponse);

            case "validate":
                log.info("Validating ticket: {}", ticketId);
                TicketResponse validateResponse = ticketService.validateAndUseTicket(ticketId);
                return ResponseEntity.ok(validateResponse);

            default:
                return ResponseEntity.badRequest().body(null);
        }
    }
}