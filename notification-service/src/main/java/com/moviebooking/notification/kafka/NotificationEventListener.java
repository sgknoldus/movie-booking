package com.moviebooking.notification.kafka;

import com.moviebooking.common.events.booking.BookingConfirmedEvent;
import com.moviebooking.notification.domain.NotificationRequest;
import com.moviebooking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.booking-confirmed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingConfirmation(BookingConfirmedEvent event) {
        log.info("Received booking confirmation event for user: {} and booking: {}", event.getUserId(), event.getBookingId());
        try {
            NotificationRequest request = createNotificationRequestFromBookingEvent(event);
            notificationService.createNotification(request);
        } catch (Exception e) {
            log.error("Error processing booking confirmation notification for booking: {}", event.getBookingId(), e);
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
    
    private NotificationRequest createNotificationRequestFromBookingEvent(BookingConfirmedEvent event) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("userId", event.getUserId());
        templateData.put("bookingId", event.getBookingId());
        templateData.put("showId", event.getShowId());
        templateData.put("theatreId", event.getTheatreId());
        templateData.put("movieId", event.getMovieId());
        templateData.put("seatNumbers", event.getSeatNumbers());
        templateData.put("totalAmount", event.getTotalAmount());
        templateData.put("showDateTime", event.getShowDateTime());
        templateData.put("confirmedAt", event.getConfirmedAt());
        
        return NotificationRequest.builder()
                .userId(java.util.UUID.randomUUID()) // Generate UUID for this notification
                .type("BOOKING_CONFIRMED")
                .subject("Booking Confirmation - Your tickets are confirmed!")
                .templateName("booking-confirmation")
                .templateData(templateData)
                .build();
    }
}
