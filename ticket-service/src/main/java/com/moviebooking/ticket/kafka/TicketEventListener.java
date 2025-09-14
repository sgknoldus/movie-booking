package com.moviebooking.ticket.kafka;

import com.moviebooking.common.events.booking.BookingConfirmedEvent;
import com.moviebooking.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventListener {

    private final TicketService ticketService;

    @KafkaListener(topics = "booking-confirmed", groupId = "ticket-service-group")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        if (event == null) {
            log.warn("Received null booking confirmed event, ignoring");
            return;
        }

        log.info("Received booking confirmed event for booking: {}", event.getBookingId());

        try {
            ticketService.createTicketFromBooking(event);
            log.info("Successfully created ticket for booking: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error creating ticket for booking: {}", event.getBookingId(), e);
            // In a real system, you might want to send this to a dead letter queue
            // or implement retry logic
        }
    }
}