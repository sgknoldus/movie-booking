package com.moviebooking.booking.application.dto;

import com.moviebooking.booking.domain.Booking;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID bookingId;
    private UUID showId;
    private UUID theatreId;
    private UUID movieId;
    private List<String> seats;
    private BigDecimal totalAmount;
    private Booking.BookingStatus status;
    private LocalDateTime bookedAt;
    private String paymentUrl;
}
