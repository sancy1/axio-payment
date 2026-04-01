package com.axioquan.payment_service.modules.notifications;

import com.axioquan.payment_service.modules.notifications.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for in-app notification management.
 * Handles creation, retrieval, and read status tracking of notifications
 * displayed in the user dashboard.
 */
public interface NotificationService {

    /**
     * Create a new notification for a user.
     * @param userId The user ID
     * @param notificationType The notification type (e.g., PAYMENT_SUCCESS, REFUND)
     * @param message The notification message
     * @param data Optional metadata as JSON string (e.g., course details, payment reference)
     * @return The created notification response
     */
    NotificationResponse createNotification(
            UUID userId,
            String notificationType,
            String message,
            String data
    );

    /**
     * Get all notifications for a user with pagination.
     * Returns notifications ordered by creation date (newest first).
     * @param userId The user ID
     * @param pageable Pagination information
     * @return Page of notification responses
     */
    Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable);

    /**
     * Mark a specific notification as read.
     * @param notificationId The notification ID
     */
    void markAsRead(UUID notificationId);

    /**
     * Mark all notifications for a user as read.
     * @param userId The user ID
     */
    void markAllAsRead(UUID userId);

    /**
     * Get count of unread notifications for a user.
     * Used for badge display in dashboard/navbar.
     * @param userId The user ID
     * @return Count of unread notifications
     */
    long getUnreadCount(UUID userId);
}

