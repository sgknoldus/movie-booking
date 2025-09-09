package com.moviebooking.common.events.booking;

import com.moviebooking.common.events.DomainEvent;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class SeatBookedEvent extends DomainEvent {
    private final UUID bookingId;
    private final UUID userId;
    private final UUID showId;
    private final List<String> seats;
    private final double totalAmount;

    public SeatBookedEvent(UUID bookingId, UUID userId, UUID showId, List<String> seats, double totalAmount) {
        super();
        this.bookingId = bookingId;
        this.userId = userId;
        this.showId = showId;
        this.seats = seats;
        this.totalAmount = totalAmount;
    }
}
