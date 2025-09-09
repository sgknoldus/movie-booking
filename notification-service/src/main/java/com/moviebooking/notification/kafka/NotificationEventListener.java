package com.moviebooking.notification.kafka;

import com.moviebooking.notification.domain.NotificationRequest;
import com.moviebooking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.booking-confirmed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingConfirmation(NotificationRequest request) {
        log.info("Received booking confirmation event for user: {}", request.getUserId());
        try {
            notificationService.createNotification(request);
        } catch (Exception e) {
            log.error("Error processing booking confirmation notification", e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.booking-cancelled}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingCancellation(NotificationRequest request) {
        log.info("Received booking cancellation event for user: {}", request.getUserId());
        try {
            notificationService.createNotification(request);
        } catch (Exception e) {
            log.error("Error processing booking cancellation notification", e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.payment-completed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentCompletion(NotificationRequest request) {
        log.info("Received payment completion event for user: {}", request.getUserId());
        try {
            notificationService.createNotification(request);
        } catch (Exception e) {
            log.error("Error processing payment completion notification", e);
        }
    }
}
