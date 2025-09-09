package com.moviebooking.notification.scheduler;

import com.moviebooking.notification.domain.Notification;
import com.moviebooking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${notification.processing.interval:60000}")
    public void processPendingNotifications() {
        log.info("Starting scheduled processing of pending notifications");
        List<Notification> pendingNotifications = notificationService.getPendingNotifications();
        
        for (Notification notification : pendingNotifications) {
            try {
                notificationService.processNotification(notification.getId());
            } catch (Exception e) {
                log.error("Error processing notification: {}", notification.getId(), e);
            }
        }
        
        log.info("Completed processing {} pending notifications", pendingNotifications.size());
    }
}
