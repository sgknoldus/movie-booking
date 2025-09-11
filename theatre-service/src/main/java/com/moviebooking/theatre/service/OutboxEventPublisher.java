package com.moviebooking.theatre.service;

import com.moviebooking.theatre.model.OutboxEvent;
import com.moviebooking.theatre.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventService outboxEventService;
    
    @Value("${app.kafka.topics.theatre-events:theatre-events}")
    private String theatreEventsTopic;
    
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventService.getPendingEvents();
        
        if (!pendingEvents.isEmpty()) {
            log.debug("Found {} pending outbox events to publish", pendingEvents.size());
        }
        
        for (OutboxEvent event : pendingEvents) {
            try {
                log.debug("Publishing outbox event: id={}, aggregateType={}, aggregateId={}, eventType={}", 
                    event.getId(), event.getAggregateType(), event.getAggregateId(), event.getEventType());
                publishEventToKafka(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event: id={}, aggregateType={}, aggregateId={}, eventType={}, error={}, stackTrace={}", 
                    event.getId(), event.getAggregateType(), event.getAggregateId(), event.getEventType(), 
                    e.getMessage(), e.getClass().getSimpleName(), e);
                outboxEventService.markEventAsFailed(event.getId());
            }
        }
    }
    
    private void publishEventToKafka(OutboxEvent event) {
        String key = event.getAggregateType() + "-" + event.getAggregateId();
        
        log.debug("Sending Kafka message: topic={}, key={}, eventId={}", theatreEventsTopic, key, event.getId());
        
        try {
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(theatreEventsTopic, key, event.getEventData());
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published outbox event {} to Kafka: topic={}, partition={}, offset={}, key={}", 
                        event.getId(), theatreEventsTopic, result.getRecordMetadata().partition(), 
                        result.getRecordMetadata().offset(), key);
                    outboxEventService.markEventAsProcessed(event.getId());
                } else {
                    log.error("Failed to publish outbox event {} to Kafka: topic={}, key={}, error={}, errorType={}, retryCount={}", 
                        event.getId(), theatreEventsTopic, key, ex.getMessage(), 
                        ex.getClass().getSimpleName(), event.getRetryCount(), ex);
                    outboxEventService.markEventAsFailed(event.getId());
                }
            });
        } catch (Exception e) {
            log.error("Exception while sending Kafka message for outbox event {}: topic={}, key={}, error={}, errorType={}", 
                event.getId(), theatreEventsTopic, key, e.getMessage(), e.getClass().getSimpleName(), e);
            outboxEventService.markEventAsFailed(event.getId());
            throw e;
        }
    }
    
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Transactional
    public void retryFailedEvents() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        List<OutboxEvent> failedEvents = outboxEventRepository.findFailedEventsForRetry(3, cutoffTime);
        
        if (!failedEvents.isEmpty()) {
            log.info("Found {} failed outbox events to retry (retry count < 3, older than 1 hour)", failedEvents.size());
        }
        
        for (OutboxEvent event : failedEvents) {
            try {
                log.info("Retrying failed outbox event: id={}, aggregateType={}, aggregateId={}, eventType={}, retryCount={}", 
                    event.getId(), event.getAggregateType(), event.getAggregateId(), event.getEventType(), event.getRetryCount());
                publishEventToKafka(event);
            } catch (Exception e) {
                log.error("Failed to retry outbox event: id={}, aggregateType={}, aggregateId={}, eventType={}, retryCount={}, error={}, errorType={}", 
                    event.getId(), event.getAggregateType(), event.getAggregateId(), event.getEventType(), 
                    event.getRetryCount(), e.getMessage(), e.getClass().getSimpleName(), e);
                outboxEventService.markEventAsFailed(event.getId());
            }
        }
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldEvents() {
        outboxEventService.cleanupProcessedEvents();
    }
}