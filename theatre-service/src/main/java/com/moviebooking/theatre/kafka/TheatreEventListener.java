package com.moviebooking.theatre.kafka;

import com.moviebooking.booking.events.BookingConfirmedEvent;
import com.moviebooking.theatre.service.SeatAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TheatreEventListener {

    private final SeatAvailabilityService seatAvailabilityService;

    @KafkaListener(topics = "booking-confirmed", groupId = "theatre-service-group")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Received booking confirmed event for show: {} with seats: {}", 
                event.getShowId(), event.getSeatNumbers());
        
        try {
            seatAvailabilityService.markSeatsAsBooked(event.getShowId(), event.getSeatNumbers());
            log.info("Successfully marked seats as booked for booking: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error marking seats as booked for booking: {}", event.getBookingId(), e);
        }
    }
}