package com.moviebooking.common.events.booking;

import com.moviebooking.common.events.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BookingConfirmedEvent extends DomainEvent {
    private final String bookingId;
    private final Long userId;
    private final Long showId;
    private final Long theatreId;
    private final Long movieId;
    private final List<String> seatNumbers;
    private final BigDecimal totalAmount;
    private final String paymentId;
    private final LocalDateTime showDateTime;
    private final LocalDateTime confirmedAt;
    private final String eventType;

    public BookingConfirmedEvent(String bookingId, Long userId, Long showId, Long theatreId,
                                 Long movieId, List<String> seatNumbers, BigDecimal totalAmount,
                                 String paymentId, LocalDateTime showDateTime, LocalDateTime confirmedAt) {
        super();
        this.bookingId = bookingId;
        this.userId = userId;
        this.showId = showId;
        this.theatreId = theatreId;
        this.movieId = movieId;
        this.seatNumbers = seatNumbers;
        this.totalAmount = totalAmount;
        this.paymentId = paymentId;
        this.showDateTime = showDateTime;
        this.confirmedAt = confirmedAt;
        this.eventType = "BOOKING_CONFIRMED";
    }
}