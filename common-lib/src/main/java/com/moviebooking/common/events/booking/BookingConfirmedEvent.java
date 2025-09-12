package com.moviebooking.common.events.booking;

import com.moviebooking.common.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookingConfirmedEvent extends DomainEvent {
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
    private String eventType = "BOOKING_CONFIRMED";
}