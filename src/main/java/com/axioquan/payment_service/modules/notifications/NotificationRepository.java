package com.axioquan.payment_service.modules.notifications;

import com.axioquan.payment_service.domain.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Notification entity.
 * Provides database queries for in-app notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a user, ordered by creation date (newest first).
     * Supports pagination for dashboard display.
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Count unread notifications for a user.
     * Used for badge count in dashboard/navbar.
     */
    long countByUserIdAndIsReadFalse(UUID userId);

    /**
     * Find unread notifications for a user.
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find notifications by type for a user.
     */
    Page<Notification> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(
            UUID userId, String notificationType, Pageable pageable);
}

