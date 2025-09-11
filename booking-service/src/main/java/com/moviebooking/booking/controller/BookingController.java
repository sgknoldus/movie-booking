package com.moviebooking.booking.controller;

import com.moviebooking.booking.dto.BookingRequest;
import com.moviebooking.booking.dto.BookingResponse;
import com.moviebooking.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking", description = "Movie ticket booking operations")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/book")
    @Operation(summary = "Book movie tickets", description = "Book tickets for a movie show with seat selection")
    public ResponseEntity<BookingResponse> bookTickets(@Valid @RequestBody BookingRequest request) {
        log.info("Received booking request for user: {}, show: {}, seats: {}", 
                request.getUserId(), request.getShowId(), request.getSeatNumbers());
        
        BookingResponse response = bookingService.bookTickets(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details", description = "Retrieve booking details by booking ID")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        log.info("Retrieving booking details for booking ID: {}", bookingId);
        
        BookingResponse response = bookingService.getBookingByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }
}