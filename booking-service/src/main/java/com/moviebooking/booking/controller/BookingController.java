package com.moviebooking.booking.controller;

import com.moviebooking.booking.dto.BookingRequest;
import com.moviebooking.booking.dto.BookingResponse;
import com.moviebooking.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Booking Management",
    description = "Comprehensive movie ticket booking system. Handles seat reservation, booking creation, retrieval, and management with real-time seat availability checks."
)
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(
        summary = "Create new movie ticket booking",
        description = "Creates a new booking for movie tickets with selected seats. Validates seat availability, calculates total price, and reserves seats for the specified show. Returns booking confirmation with payment details."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Booking created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookingResponse.class),
                examples = @ExampleObject(
                    name = "Successful Booking",
                    value = "{\"bookingId\":\"BK123456789\",\"userId\":1,\"showId\":101,\"seatNumbers\":[\"A1\",\"A2\"],\"totalAmount\":500.00,\"status\":\"CONFIRMED\",\"bookingTime\":\"2024-01-15T14:30:00Z\",\"expiryTime\":\"2024-01-15T14:45:00Z\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid booking data or seat validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
        @ApiResponse(responseCode = "409", description = "Conflict - Selected seats are no longer available"),
        @ApiResponse(responseCode = "422", description = "Unprocessable Entity - Show not available for booking")
    })
    public ResponseEntity<BookingResponse> bookTickets(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Booking request containing show details, seat selection, and user information",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookingRequest.class),
                    examples = @ExampleObject(
                        name = "Booking Request Example",
                        value = "{\"userId\":1,\"showId\":101,\"seatNumbers\":[\"A1\",\"A2\"],\"couponCode\":\"SAVE20\"}"
                    )
                )
            )
            @Valid @RequestBody BookingRequest request) {
        log.info("Received booking request for user: {}, show: {}, seats: {}",
                request.getUserId(), request.getShowId(), request.getSeatNumbers());

        BookingResponse response = bookingService.bookTickets(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bookingId}")
    @Operation(
        summary = "Retrieve booking details",
        description = "Fetches complete booking information including show details, seat information, payment status, and ticket generation status. Users can only access their own bookings unless they have admin privileges."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Booking details retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookingResponse.class),
                examples = @ExampleObject(
                    name = "Booking Details Example",
                    value = "{\"bookingId\":\"BK123456789\",\"userId\":1,\"showId\":101,\"movieTitle\":\"Avengers: Endgame\",\"theatreName\":\"PVR Cinemas\",\"screenName\":\"Screen 1\",\"showDateTime\":\"2024-01-15T19:30:00Z\",\"seatNumbers\":[\"A1\",\"A2\"],\"totalAmount\":500.00,\"status\":\"CONFIRMED\",\"paymentStatus\":\"PAID\",\"bookingTime\":\"2024-01-15T14:30:00Z\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot access another user's booking"),
        @ApiResponse(responseCode = "404", description = "Booking not found with the provided ID")
    })
    public ResponseEntity<BookingResponse> getBooking(
            @Parameter(
                description = "Unique booking identifier (alphanumeric)",
                example = "BK123456789",
                required = true
            )
            @PathVariable String bookingId) {
        log.info("Retrieving booking details for booking ID: {}", bookingId);

        BookingResponse response = bookingService.getBookingByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }
}