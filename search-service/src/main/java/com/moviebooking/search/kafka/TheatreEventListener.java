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
                   groupId = "search-service-group",
                   containerFactory = "stringKafkaListenerContainerFactory")
    public void handleTheatreEvent(@Payload String eventData, 
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        
        log.info("Received Kafka event: topic={}, partition={}, offset={}, key={}, dataLength={}", 
                topic, partition, offset, key, eventData != null ? eventData.length() : 0);
        
        try {
            if (eventData == null || eventData.trim().isEmpty()) {
                log.error("Received empty or null event data: topic={}, partition={}, offset={}, key={}", 
                    topic, partition, offset, key);
                return;
            }
            
            JsonNode eventJson = objectMapper.readTree(eventData);
            String aggregateType = extractAggregateType(key);
            String eventType = determineEventType(eventJson);
            
            log.debug("Processing event: aggregateType={}, eventType={}, topic={}, key={}", 
                aggregateType, eventType, topic, key);
            
            switch (aggregateType) {
                case "City" -> handleCityEvent(eventType, eventJson, topic, partition, offset, key);
                case "Theatre" -> handleTheatreEvent(eventType, eventJson, topic, partition, offset, key);
                case "Screen" -> handleScreenEvent(eventType, eventJson, topic, partition, offset, key);
                case "Show" -> handleShowEvent(eventType, eventJson, topic, partition, offset, key);
                case "SeatAvailability" -> handleSeatEvent(eventType, eventJson, topic, partition, offset, key);
                default -> {
                    log.warn("Unknown aggregate type: {} for event: topic={}, partition={}, offset={}, key={}", 
                        aggregateType, topic, partition, offset, key);
                }
            }
            
            log.debug("Successfully processed event: aggregateType={}, eventType={}, topic={}, key={}", 
                aggregateType, eventType, topic, key);
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse JSON event data: topic={}, partition={}, offset={}, key={}, error={}, eventData={}", 
                topic, partition, offset, key, e.getMessage(), eventData, e);
        } catch (Exception e) {
            log.error("Failed to process Kafka event: topic={}, partition={}, offset={}, key={}, error={}, errorType={}, eventData={}", 
                topic, partition, offset, key, e.getMessage(), e.getClass().getSimpleName(), eventData, e);
            // TODO: In a production system, consider sending this to a dead letter queue
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
    
    private void handleCityEvent(String eventType, JsonNode eventData, String topic, int partition, long offset, String key) {
        try {
            String cityId = eventData.has("id") ? eventData.get("id").asText() : "unknown";
            log.debug("Handling city event: eventType={}, cityId={}, topic={}, key={}",
                eventType, cityId, topic, key);

            // Validate required fields for city events
            if (!validateCityEventData(eventData, eventType)) {
                log.error("Invalid city event data: eventType={}, cityId={}, topic={}, key={}, missingFields=true",
                    eventType, cityId, topic, key);
                return;
            }

            switch (eventType) {
                case "UPSERT":
                case "CREATED":
                case "UPDATED":
                    searchIndexService.indexCity(eventData);
                    log.info("Successfully indexed city: id={}, eventType={}, topic={}, key={}", 
                        eventData.has("id") ? eventData.get("id").asText() : "unknown", eventType, topic, key);
                    break;
                case "DELETED":
                    searchIndexService.deleteCity(cityId);
                    log.info("Successfully deleted city from index: id={}, topic={}, key={}", cityId, topic, key);
                    break;
                default:
                    log.warn("Unknown city event type: {} for city: id={}, topic={}, key={}", 
                        eventType, eventData.has("id") ? eventData.get("id").asText() : "unknown", topic, key);
            }
        } catch (Exception e) {
            log.error("Failed to handle city event: eventType={}, cityId={}, topic={}, partition={}, offset={}, key={}, error={}, errorType={}", 
                eventType, eventData.has("id") ? eventData.get("id").asText() : "unknown", 
                topic, partition, offset, key, e.getMessage(), e.getClass().getSimpleName(), e);
        }
    }
    
    private void handleTheatreEvent(String eventType, JsonNode eventData, String topic, int partition, long offset, String key) {
        try {
            String theatreId = eventData.has("id") ? eventData.get("id").asText() : "unknown";
            log.debug("Handling theatre event: eventType={}, theatreId={}, topic={}, key={}",
                eventType, theatreId, topic, key);

            // Validate required fields for theatre events
            if (!validateTheatreEventData(eventData, eventType)) {
                log.error("Invalid theatre event data: eventType={}, theatreId={}, topic={}, key={}, missingFields=true",
                    eventType, theatreId, topic, key);
                return;
            }

            switch (eventType) {
                case "UPSERT":
                case "CREATED":
                case "UPDATED":
                    searchIndexService.indexTheatre(eventData);
                    log.info("Successfully indexed theatre: id={}, eventType={}, topic={}, key={}",
                        theatreId, eventType, topic, key);
                    break;
                case "DELETED":
                    searchIndexService.deleteTheatre(theatreId);
                    log.info("Successfully deleted theatre from index: id={}, topic={}, key={}", theatreId, topic, key);
                    break;
                default:
                    log.warn("Unknown theatre event type: {} for theatre: id={}, topic={}, key={}",
                        eventType, theatreId, topic, key);
            }
        } catch (Exception e) {
            log.error("Failed to handle theatre event: eventType={}, theatreId={}, topic={}, partition={}, offset={}, key={}, error={}, errorType={}",
                eventType, eventData.has("id") ? eventData.get("id").asText() : "unknown",
                topic, partition, offset, key, e.getMessage(), e.getClass().getSimpleName(), e);
        }
    }
    
    private void handleScreenEvent(String eventType, JsonNode eventData, String topic, int partition, long offset, String key) {
        // Screen events might affect theatre documents if we store screen count
        try {
            String screenId = eventData.has("id") ? eventData.get("id").asText() : "unknown";
            log.info("Received screen event: eventType={}, screenId={}, topic={}, key={}", eventType, screenId, topic, key);
            
            // TODO: Implement screen event handling if needed for search indexing
            log.debug("Screen event processed (no action taken): eventType={}, screenId={}, topic={}, key={}", 
                eventType, screenId, topic, key);
        } catch (Exception e) {
            log.error("Failed to handle screen event: eventType={}, topic={}, partition={}, offset={}, key={}, error={}, errorType={}", 
                eventType, topic, partition, offset, key, e.getMessage(), e.getClass().getSimpleName(), e);
        }
    }
    
    private void handleShowEvent(String eventType, JsonNode eventData, String topic, int partition, long offset, String key) {
        try {
            String showId = eventData.has("id") ? eventData.get("id").asText() : "unknown";
            log.debug("Handling show event: eventType={}, showId={}, topic={}, key={}",
                eventType, showId, topic, key);

            // Validate required fields for show events
            if (!validateShowEventData(eventData, eventType)) {
                log.error("Invalid show event data: eventType={}, showId={}, topic={}, key={}, missingFields=true",
                    eventType, showId, topic, key);
                return;
            }

            switch (eventType) {
                case "UPSERT":
                case "CREATED":
                case "UPDATED":
                    searchIndexService.indexShow(eventData);
                    log.info("Successfully indexed show: id={}, eventType={}, topic={}, key={}", 
                        eventData.has("id") ? eventData.get("id").asText() : "unknown", eventType, topic, key);
                    break;
                case "DELETED":
                    searchIndexService.deleteShow(showId);
                    log.info("Successfully deleted show from index: id={}, topic={}, key={}", showId, topic, key);
                    break;
                default:
                    log.warn("Unknown show event type: {} for show: id={}, topic={}, key={}", 
                        eventType, eventData.has("id") ? eventData.get("id").asText() : "unknown", topic, key);
            }
        } catch (Exception e) {
            log.error("Failed to handle show event: eventType={}, showId={}, topic={}, partition={}, offset={}, key={}, error={}, errorType={}", 
                eventType, eventData.has("id") ? eventData.get("id").asText() : "unknown", 
                topic, partition, offset, key, e.getMessage(), e.getClass().getSimpleName(), e);
        }
    }
    
    private void handleSeatEvent(String eventType, JsonNode eventData, String topic, int partition, long offset, String key) {
        // Seat availability changes might affect show documents (available seats count)
        try {
            String seatId = eventData.has("id") ? eventData.get("id").asText() : "unknown";
            Long showId = eventData.has("showId") ? eventData.get("showId").asLong() : null;
            
            log.debug("Handling seat event: eventType={}, seatId={}, showId={}, topic={}, key={}", 
                eventType, seatId, showId, topic, key);
            
            if (showId != null) {
                searchIndexService.updateShowAvailableSeats(showId);
                log.info("Successfully updated show available seats: seatId={}, showId={}, eventType={}, topic={}, key={}", 
                    seatId, showId, eventType, topic, key);
            } else {
                log.warn("Seat event missing showId: seatId={}, eventType={}, topic={}, key={}", 
                    seatId, eventType, topic, key);
            }
        } catch (Exception e) {
            log.error("Failed to handle seat event: eventType={}, seatId={}, topic={}, partition={}, offset={}, key={}, error={}, errorType={}", 
                eventType, eventData.has("id") ? eventData.get("id").asText() : "unknown", 
                topic, partition, offset, key, e.getMessage(), e.getClass().getSimpleName(), e);
        }
    }

    private boolean validateTheatreEventData(JsonNode eventData, String eventType) {
        // For delete events, we only need ID
        if ("DELETED".equals(eventType)) {
            return eventData.has("id") && !eventData.get("id").isNull();
        }

        // For create/update/upsert events, validate required fields
        boolean hasId = eventData.has("id") && !eventData.get("id").isNull();
        boolean hasName = eventData.has("name") && !eventData.get("name").isNull();
        boolean hasAddress = eventData.has("address") && !eventData.get("address").isNull();
        boolean hasCityId = eventData.has("cityId") && !eventData.get("cityId").isNull();
        boolean hasCityName = eventData.has("cityName") && !eventData.get("cityName").isNull();

        if (!hasId || !hasName || !hasAddress || !hasCityId || !hasCityName) {
            log.warn("Theatre event missing required fields: id={}, name={}, address={}, cityId={}, cityName={}",
                hasId, hasName, hasAddress, hasCityId, hasCityName);
            return false;
        }

        return true;
    }

    private boolean validateCityEventData(JsonNode eventData, String eventType) {
        // For delete events, we only need ID
        if ("DELETED".equals(eventType)) {
            return eventData.has("id") && !eventData.get("id").isNull();
        }

        // For create/update/upsert events, validate required fields
        boolean hasId = eventData.has("id") && !eventData.get("id").isNull();
        boolean hasName = eventData.has("name") && !eventData.get("name").isNull();
        boolean hasState = eventData.has("state") && !eventData.get("state").isNull();
        boolean hasCountry = eventData.has("country") && !eventData.get("country").isNull();

        if (!hasId || !hasName || !hasState || !hasCountry) {
            log.warn("City event missing required fields: id={}, name={}, state={}, country={}",
                hasId, hasName, hasState, hasCountry);
            return false;
        }

        return true;
    }

    private boolean validateShowEventData(JsonNode eventData, String eventType) {
        // For delete events, we only need ID
        if ("DELETED".equals(eventType)) {
            return eventData.has("id") && !eventData.get("id").isNull();
        }

        // For create/update/upsert events, validate required fields
        boolean hasId = eventData.has("id") && !eventData.get("id").isNull();
        boolean hasMovieId = eventData.has("movieId") && !eventData.get("movieId").isNull();
        boolean hasMovieTitle = eventData.has("movieTitle") && !eventData.get("movieTitle").isNull();
        boolean hasTheatreId = eventData.has("theatreId") && !eventData.get("theatreId").isNull();
        boolean hasTheatreName = eventData.has("theatreName") && !eventData.get("theatreName").isNull();

        if (!hasId || !hasMovieId || !hasMovieTitle || !hasTheatreId || !hasTheatreName) {
            log.warn("Show event missing required fields: id={}, movieId={}, movieTitle={}, theatreId={}, theatreName={}",
                hasId, hasMovieId, hasMovieTitle, hasTheatreId, hasTheatreName);
            return false;
        }

        return true;
    }
}