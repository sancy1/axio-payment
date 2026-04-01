package com.axioquan.payment_service.modules.notifications.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for in-app notification response.
 * Displays notification in user dashboard with read/unread status.
 */
@JsonPropertyOrder({
    "id",
    "userId",
    "notificationType",
    "title",
    "message",
    "isRead",
    "actionUrl",
    "iconType",
    "data",
    "createdAt",
    "readAt"
})
public record NotificationResponse(
        UUID id,
        UUID userId,
        String notificationType,
        String title,
        String message,
        Boolean isRead,
        String actionUrl,
        String iconType,
        String data,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {}
