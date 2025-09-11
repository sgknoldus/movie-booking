package com.moviebooking.theatre.events;

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
public class BookingConfirmedEvent {
    private String bookingId;
    private Long userId;
    private Long showId;
    private Long theatreId;
    private Long movieId;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private String paymentId;
    private LocalDateTime showDateTime;
    private LocalDateTime confirmedAt;
    private String eventType;
}