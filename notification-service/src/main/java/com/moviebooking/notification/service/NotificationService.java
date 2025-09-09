package com.moviebooking.notification.service;

import com.moviebooking.notification.domain.Notification;
import com.moviebooking.notification.domain.NotificationRequest;
import com.moviebooking.notification.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public Notification createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .subject(request.getSubject())
                .recipientEmail(request.getRecipientEmail())
                .recipientPhone(request.getRecipientPhone())
                .status(Notification.NotificationStatus.PENDING)
                .build();

        // Process template if template name is provided
        if (request.getTemplateName() != null && request.getTemplateData() != null) {
            Context context = new Context();
            context.setVariables(request.getTemplateData());
            String content = templateEngine.process(request.getTemplateName(), context);
            notification.setContent(content);
        }

        return notificationRepository.save(notification);
    }

    public void sendEmailNotification(Notification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getContent(), true);

            mailSender.send(message);

            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Email notification sent successfully to: {}", notification.getRecipientEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email notification", e);
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByStatus(Notification.NotificationStatus.PENDING);
    }

    public void processNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getRecipientEmail() != null) {
            sendEmailNotification(notification);
        }

        // Add SMS sending logic here if needed
    }
}
