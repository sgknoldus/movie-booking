package com.moviebooking.search.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheatreEventListener {
    private final ObjectMapper objectMapper;
    private final SearchIndexService searchIndexService;
    
    @KafkaListener(topics = "${app.kafka.topics.theatre-events:theatre-events}", 
                   groupId = "search-service-group")
    @Transactional("transactionManager")
    public void handleTheatreEvent(@Payload String eventData, 
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        
        log.info("Received event from topic: {}, partition: {}, offset: {}, key: {}", 
                topic, partition, offset, key);
        
        try {
            JsonNode eventJson = objectMapper.readTree(eventData);
            String aggregateType = extractAggregateType(key);
            String eventType = determineEventType(eventJson);
            
            switch (aggregateType) {
                case "City" -> handleCityEvent(eventType, eventJson);
                case "Theatre" -> handleTheatreEvent(eventType, eventJson);
                case "Screen" -> handleScreenEvent(eventType, eventJson);
                case "Show" -> handleShowEvent(eventType, eventJson);
                case "SeatAvailability" -> handleSeatEvent(eventType, eventJson);
                default -> log.warn("Unknown aggregate type: {}", aggregateType);
            }
            
        } catch (Exception e) {
            log.error("Failed to process event: {}", e.getMessage(), e);
            // In a production system, you might want to send this to a dead letter queue
        }
    }
    
    private String extractAggregateType(String key) {
        if (key != null && key.contains("-")) {
            return key.split("-")[0];
        }
        return "Unknown";
    }
    
    private String determineEventType(JsonNode eventJson) {
        // This is a simplified approach. In a real system, you might have an eventType field
        // or use different topics for different event types
        if (eventJson.has("id") && eventJson.has("name")) {
            return "UPSERT";
        }
        return "UNKNOWN";
    }
    
    private void handleCityEvent(String eventType, JsonNode eventData) {
        try {
            switch (eventType) {
                case "UPSERT":
                case "CREATED":
                case "UPDATED":
                    searchIndexService.indexCity(eventData);
                    break;
                case "DELETED":
                    searchIndexService.deleteCity(eventData.get("id").asText());
                    break;
                default:
                    log.warn("Unknown city event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to handle city event: {}", e.getMessage(), e);
        }
    }
    
    private void handleTheatreEvent(String eventType, JsonNode eventData) {
        try {
            switch (eventType) {
                case "UPSERT":
                case "CREATED":
                case "UPDATED":
                    searchIndexService.indexTheatre(eventData);
                    break;
                case "DELETED":
                    searchIndexService.deleteTheatre(eventData.get("id").asText());
                    break;
                default:
                    log.warn("Unknown theatre event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to handle theatre event: {}", e.getMessage(), e);
        }
    }
    
    private void handleScreenEvent(String eventType, JsonNode eventData) {
        // Screen events might affect theatre documents if we store screen count
        log.info("Received screen event: {} for screen ID: {}", eventType, eventData.get("id").asText());
    }
    
    private void handleShowEvent(String eventType, JsonNode eventData) {
        try {
            switch (eventType) {
                case "UPSERT":
                case "CREATED":
                case "UPDATED":
                    searchIndexService.indexShow(eventData);
                    break;
                case "DELETED":
                    searchIndexService.deleteShow(eventData.get("id").asText());
                    break;
                default:
                    log.warn("Unknown show event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to handle show event: {}", e.getMessage(), e);
        }
    }
    
    private void handleSeatEvent(String eventType, JsonNode eventData) {
        // Seat availability changes might affect show documents (available seats count)
        try {
            Long showId = eventData.get("showId").asLong();
            searchIndexService.updateShowAvailableSeats(showId);
        } catch (Exception e) {
            log.error("Failed to handle seat event: {}", e.getMessage(), e);
        }
    }
}