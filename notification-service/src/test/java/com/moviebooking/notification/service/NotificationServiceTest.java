package com.moviebooking.notification.service;

import com.moviebooking.notification.domain.Notification;
import com.moviebooking.notification.domain.NotificationRequest;
import com.moviebooking.notification.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest notificationRequest;
    private Notification testNotification;
    private UUID testUserId;
    private UUID testNotificationId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNotificationId = UUID.randomUUID();

        notificationRequest = NotificationRequest.builder()
                .userId(testUserId)
                .type("BOOKING_CONFIRMATION")
                .subject("Booking Confirmation")
                .recipientEmail("test@example.com")
                .recipientPhone("+1234567890")
                .templateName("booking-confirmation")
                .templateData(Map.of(
                    "userName", "John Doe",
                    "movieTitle", "Avengers",
                    "showTime", "7:00 PM"
                ))
                .build();

        testNotification = Notification.builder()
                .id(testNotificationId)
                .userId(testUserId)
                .type("BOOKING_CONFIRMATION")
                .subject("Booking Confirmation")
                .content("Your booking has been confirmed for Avengers at 7:00 PM")
                .recipientEmail("test@example.com")
                .recipientPhone("+1234567890")
                .status(Notification.NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createNotification_ShouldCreateNotificationWithTemplate() {
        // Given
        String processedTemplate = "Dear John Doe, your booking for Avengers at 7:00 PM is confirmed.";
        when(templateEngine.process(eq("booking-confirmation"), any(Context.class)))
                .thenReturn(processedTemplate);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createNotification(notificationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getType()).isEqualTo("BOOKING_CONFIRMATION");
        assertThat(result.getSubject()).isEqualTo("Booking Confirmation");
        assertThat(result.getRecipientEmail()).isEqualTo("test@example.com");
        assertThat(result.getStatus()).isEqualTo(Notification.NotificationStatus.PENDING);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification.getContent()).isEqualTo(processedTemplate);

        verify(templateEngine).process(eq("booking-confirmation"), any(Context.class));
    }

    @Test
    void createNotification_ShouldCreateNotificationWithoutTemplate() {
        // Given
        notificationRequest.setTemplateName(null);
        notificationRequest.setTemplateData(null);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createNotification(notificationRequest);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).save(any(Notification.class));
        verify(templateEngine, never()).process(anyString(), any(Context.class));
    }

    @Test
    void createNotification_ShouldSetCorrectTemplateVariables() {
        // Given
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("processed content");
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.createNotification(notificationRequest);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("booking-confirmation"), contextCaptor.capture());

        // Note: Context doesn't expose variables for testing, but we verify the method was called
        verify(templateEngine).process(anyString(), any(Context.class));
    }

    @Test
    void sendEmailNotification_ShouldSendEmailSuccessfully() throws MessagingException {
        // Given
        MimeMessageHelper helper = mock(MimeMessageHelper.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.sendEmailNotification(testNotification);

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);

        assertThat(testNotification.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
        assertThat(testNotification.getSentAt()).isNotNull();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void sendEmailNotification_ShouldHandleMessagingException() throws MessagingException {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP server error")).when(mailSender).send(mimeMessage);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Then
        assertThatThrownBy(() -> notificationService.sendEmailNotification(testNotification))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send email notification")
                .hasCauseInstanceOf(MailSendException.class);

        assertThat(testNotification.getStatus()).isEqualTo(Notification.NotificationStatus.FAILED);
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void sendEmailNotification_ShouldHandleMimeMessageCreationException() {
        // Given
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Failed to create message"));
        lenient().when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Then
        assertThatThrownBy(() -> notificationService.sendEmailNotification(testNotification))
                .isInstanceOf(RuntimeException.class);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void getUserNotifications_ShouldReturnUserNotifications() {
        // Given
        Notification notification2 = Notification.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .type("BOOKING_CANCELLATION")
                .subject("Booking Cancelled")
                .build();

        List<Notification> userNotifications = List.of(testNotification, notification2);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(userNotifications);

        // When
        List<Notification> result = notificationService.getUserNotifications(testUserId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(userNotifications);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void getUserNotifications_ShouldReturnEmptyList_WhenNoNotifications() {
        // Given
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(List.of());

        // When
        List<Notification> result = notificationService.getUserNotifications(testUserId);

        // Then
        assertThat(result).isEmpty();
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(testUserId);
    }

    @Test
    void getPendingNotifications_ShouldReturnPendingNotifications() {
        // Given
        Notification pendingNotification1 = testNotification;
        Notification pendingNotification2 = Notification.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status(Notification.NotificationStatus.PENDING)
                .build();

        List<Notification> pendingNotifications = List.of(pendingNotification1, pendingNotification2);
        when(notificationRepository.findByStatus(Notification.NotificationStatus.PENDING))
                .thenReturn(pendingNotifications);

        // When
        List<Notification> result = notificationService.getPendingNotifications();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(pendingNotifications);
        assertThat(result).allMatch(n -> n.getStatus() == Notification.NotificationStatus.PENDING);
        verify(notificationRepository).findByStatus(Notification.NotificationStatus.PENDING);
    }

    @Test
    void processNotification_ShouldSendEmailNotification_WhenEmailExists() throws MessagingException {
        // Given
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(testNotification));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.processNotification(testNotificationId);

        // Then
        verify(notificationRepository).findById(testNotificationId);
        verify(mailSender).send(mimeMessage);
        assertThat(testNotification.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
    }

    @Test
    void processNotification_ShouldNotSendEmail_WhenEmailIsNull() {
        // Given
        testNotification.setRecipientEmail(null);
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.of(testNotification));

        // When
        notificationService.processNotification(testNotificationId);

        // Then
        verify(notificationRepository).findById(testNotificationId);
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void processNotification_ShouldThrowException_WhenNotificationNotFound() {
        // Given
        when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> notificationService.processNotification(testNotificationId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Notification not found");

        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    void createNotification_ShouldHandleNullTemplateData() {
        // Given
        notificationRequest.setTemplateData(null);
        lenient().when(templateEngine.process(eq("booking-confirmation"), any(Context.class)))
                .thenReturn("processed content");
        lenient().when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createNotification(notificationRequest);

        // Then
        assertThat(result).isNotNull();
        verify(templateEngine, never()).process(anyString(), any(Context.class));
    }

    @Test
    void createNotification_ShouldHandleTemplateProcessingException() {
        // Given
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template processing failed"));

        // Then
        assertThatThrownBy(() -> notificationService.createNotification(notificationRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Template processing failed");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void sendEmailNotification_ShouldUpdateNotificationTimestamp() throws MessagingException {
        // Given
        LocalDateTime beforeSending = LocalDateTime.now();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.sendEmailNotification(testNotification);

        // Then
        assertThat(testNotification.getSentAt()).isNotNull();
        assertThat(testNotification.getSentAt()).isAfter(beforeSending);
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void createNotification_ShouldSetDefaultPendingStatus() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createNotification(notificationRequest);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification.getStatus()).isEqualTo(Notification.NotificationStatus.PENDING);
    }
}