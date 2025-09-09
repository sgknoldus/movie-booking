package com.moviebooking.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private UUID userId;
    private String type;
    private String subject;
    private String templateName;
    private Map<String, Object> templateData;
    private String recipientEmail;
    private String recipientPhone;
}
