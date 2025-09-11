package com.moviebooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityResponse {
    private boolean available;
    private List<String> availableSeats;
    private List<String> unavailableSeats;
    private BigDecimal pricePerSeat;
    private BigDecimal totalPrice;
    private Long theatreId;
    private Long movieId;
    private LocalDateTime showDateTime;
    private String message;
}