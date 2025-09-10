package com.moviebooking.booking.presentation.controller;

import com.moviebooking.booking.application.dto.BookingRequest;
import com.moviebooking.booking.application.dto.BookingResponse;
import com.moviebooking.booking.application.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs for managing movie ticket bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) throws Exception {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details by ID")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID bookingId) {
        BookingResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all bookings for a user")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable UUID userId) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{bookingId}")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{bookingId}/payment-status")
    @Operation(summary = "Update payment status for a booking")
    public ResponseEntity<Void> updatePaymentStatus(
            @PathVariable UUID bookingId,
            @RequestBody PaymentStatusUpdate update) {
        bookingService.updatePaymentStatus(bookingId, update.status());
        return ResponseEntity.ok().build();
    }

    public record PaymentStatusUpdate(String status) {}
}
