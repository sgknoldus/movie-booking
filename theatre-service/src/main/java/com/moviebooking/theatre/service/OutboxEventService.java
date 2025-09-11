package com.moviebooking.theatre.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.theatre.model.*;
import com.moviebooking.theatre.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void publishCityEvent(String eventType, City city) {
        publishEvent(city.getId().toString(), "City", eventType, createCityEventData(city));
    }
    
    @Transactional
    public void publishTheatreEvent(String eventType, Theatre theatre) {
        publishEvent(theatre.getId().toString(), "Theatre", eventType, createTheatreEventData(theatre));
    }
    
    @Transactional
    public void publishScreenEvent(String eventType, Screen screen) {
        publishEvent(screen.getId().toString(), "Screen", eventType, createScreenEventData(screen));
    }
    
    @Transactional
    public void publishShowEvent(String eventType, Show show) {
        publishEvent(show.getId().toString(), "Show", eventType, createShowEventData(show));
    }
    
    @Transactional
    public void publishSeatAvailabilityEvent(String eventType, SeatAvailability seat) {
        publishEvent(seat.getId().toString(), "SeatAvailability", eventType, createSeatEventData(seat));
    }
    
    private void publishEvent(String aggregateId, String aggregateType, String eventType, Map<String, Object> eventData) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setAggregateId(aggregateId);
            event.setAggregateType(aggregateType);
            event.setEventType(eventType);
            event.setEventData(objectMapper.writeValueAsString(eventData));
            event.setStatus(OutboxEvent.EventStatus.PENDING);
            
            outboxEventRepository.save(event);
            log.info("Outbox event created: {} for aggregate: {} ({})", eventType, aggregateType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event data for {}: {}", eventType, e.getMessage());
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
    
    public List<OutboxEvent> getPendingEvents() {
        return outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxEvent.EventStatus.PENDING);
    }
    
    @Transactional
    public void markEventAsProcessed(Long eventId) {
        OutboxEvent event = outboxEventRepository.findById(eventId).orElse(null);
        if (event != null) {
            event.setStatus(OutboxEvent.EventStatus.PROCESSED);
            event.setProcessedAt(LocalDateTime.now());
            outboxEventRepository.save(event);
        }
    }
    
    @Transactional
    public void markEventAsFailed(Long eventId) {
        OutboxEvent event = outboxEventRepository.findById(eventId).orElse(null);
        if (event != null) {
            int newRetryCount = event.getRetryCount() + 1;
            event.setStatus(OutboxEvent.EventStatus.FAILED);
            event.setRetryCount(newRetryCount);
            outboxEventRepository.save(event);
            
            log.warn("Marked outbox event as FAILED: id={}, aggregateType={}, aggregateId={}, eventType={}, retryCount={}", 
                eventId, event.getAggregateType(), event.getAggregateId(), event.getEventType(), newRetryCount);
            
            if (newRetryCount >= 3) {
                log.error("Outbox event {} has reached maximum retry count (3), will not be retried automatically. Manual intervention required.", eventId);
            }
        } else {
            log.error("Failed to mark outbox event as failed - event not found: id={}", eventId);
        }
    }
    
    @Async
    @Transactional
    public void cleanupProcessedEvents() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);
        outboxEventRepository.deleteByStatusAndProcessedAtBefore(OutboxEvent.EventStatus.PROCESSED, cutoffTime);
        log.info("Cleaned up processed outbox events older than {}", cutoffTime);
    }
    
    private Map<String, Object> createCityEventData(City city) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", city.getId());
        data.put("name", city.getName());
        data.put("state", city.getState());
        data.put("country", city.getCountry());
        data.put("zipCode", city.getZipCode());
        data.put("createdAt", city.getCreatedAt());
        data.put("updatedAt", city.getUpdatedAt());
        return data;
    }
    
    private Map<String, Object> createTheatreEventData(Theatre theatre) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", theatre.getId());
        data.put("name", theatre.getName());
        data.put("address", theatre.getAddress());
        data.put("phoneNumber", theatre.getPhoneNumber());
        data.put("email", theatre.getEmail());
        data.put("latitude", theatre.getLatitude());
        data.put("longitude", theatre.getLongitude());
        data.put("cityId", theatre.getCity().getId());
        data.put("cityName", theatre.getCity().getName());
        data.put("createdAt", theatre.getCreatedAt());
        data.put("updatedAt", theatre.getUpdatedAt());
        return data;
    }
    
    private Map<String, Object> createScreenEventData(Screen screen) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", screen.getId());
        data.put("name", screen.getName());
        data.put("totalSeats", screen.getTotalSeats());
        data.put("screenType", screen.getScreenType());
        data.put("theatreId", screen.getTheatre().getId());
        data.put("theatreName", screen.getTheatre().getName());
        data.put("createdAt", screen.getCreatedAt());
        data.put("updatedAt", screen.getUpdatedAt());
        return data;
    }
    
    private Map<String, Object> createShowEventData(Show show) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", show.getId());
        data.put("movieId", show.getMovieId());
        data.put("movieTitle", show.getMovieTitle());
        data.put("showDateTime", show.getShowDateTime());
        data.put("endDateTime", show.getEndDateTime());
        data.put("price", show.getPrice());
        data.put("availableSeats", show.getAvailableSeats());
        data.put("status", show.getStatus());
        data.put("screenId", show.getScreen().getId());
        data.put("screenName", show.getScreen().getName());
        data.put("theatreId", show.getTheatre().getId());
        data.put("theatreName", show.getTheatre().getName());
        data.put("cityId", show.getTheatre().getCity().getId());
        data.put("cityName", show.getTheatre().getCity().getName());
        data.put("createdAt", show.getCreatedAt());
        data.put("updatedAt", show.getUpdatedAt());
        return data;
    }
    
    private Map<String, Object> createSeatEventData(SeatAvailability seat) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", seat.getId());
        data.put("seatNumber", seat.getSeatNumber());
        data.put("rowNumber", seat.getRowNumber());
        data.put("seatType", seat.getSeatType());
        data.put("status", seat.getStatus());
        data.put("bookingId", seat.getBookingId());
        data.put("lockedUntil", seat.getLockedUntil());
        data.put("showId", seat.getShow().getId());
        data.put("createdAt", seat.getCreatedAt());
        data.put("updatedAt", seat.getUpdatedAt());
        return data;
    }
}