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
        
        for (OutboxEvent event : pendingEvents) {
            try {
                publishEventToKafka(event);
            } catch (Exception e) {
                log.error("Failed to publish event {}: {}", event.getId(), e.getMessage());
                outboxEventService.markEventAsFailed(event.getId());
            }
        }
    }
    
    private void publishEventToKafka(OutboxEvent event) {
        String key = event.getAggregateType() + "-" + event.getAggregateId();
        
        CompletableFuture<SendResult<String, String>> future = 
            kafkaTemplate.send(theatreEventsTopic, key, event.getEventData());
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published event {} to Kafka: offset={}", 
                    event.getId(), result.getRecordMetadata().offset());
                outboxEventService.markEventAsProcessed(event.getId());
            } else {
                log.error("Failed to publish event {} to Kafka: {}", event.getId(), ex.getMessage());
                outboxEventService.markEventAsFailed(event.getId());
            }
        });
    }
    
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Transactional
    public void retryFailedEvents() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        List<OutboxEvent> failedEvents = outboxEventRepository.findFailedEventsForRetry(3, cutoffTime);
        
        for (OutboxEvent event : failedEvents) {
            try {
                publishEventToKafka(event);
                log.info("Retrying failed event {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to retry event {}: {}", event.getId(), e.getMessage());
                outboxEventService.markEventAsFailed(event.getId());
            }
        }
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldEvents() {
        outboxEventService.cleanupProcessedEvents();
    }
}