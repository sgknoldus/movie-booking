package com.moviebooking.search.kafka;

import com.moviebooking.search.events.BookingConfirmedEvent;
import com.moviebooking.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchEventListener {

    private final SearchIndexService searchIndexService;

    @KafkaListener(topics = "booking-confirmed", groupId = "search-service-group")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Received booking confirmed event for show: {} with seats: {}", 
                event.getShowId(), event.getSeatNumbers());
        
        try {
            searchIndexService.updateShowSeatAvailability(event.getShowId(), event.getSeatNumbers(), false);
            log.info("Successfully updated search index for booking: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error updating search index for booking: {}", event.getBookingId(), e);
        }
    }
}