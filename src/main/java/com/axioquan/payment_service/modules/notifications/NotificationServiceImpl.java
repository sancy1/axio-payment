package com.axioquan.payment_service.modules.notifications;

import com.axioquan.payment_service.domain.entities.Notification;
import com.axioquan.payment_service.modules.notifications.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of NotificationService.
 * Provides business logic for in-app notification management.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    public NotificationResponse createNotification(UUID userId, String notificationType, String message, String data) {
        log.info("Creating notification for user: {} of type: {}", userId, notificationType);

        Notification notification = Notification.builder()
                .userId(userId)
                .notificationType(notificationType)
                .title(resolveTitleFromType(notificationType))
                .message(message)
                .isRead(false)
                .iconType(resolveIconType(notificationType))
                .data(data)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with ID: {} for user: {}", saved.getId(), userId);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        log.debug("Fetching notifications for user: {} with pagination", userId);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToResponse);
    }

    @Override
    public void markAsRead(UUID notificationId) {
        log.debug("Marking notification as read: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        notification.markAsRead();
        notificationRepository.save(notification);

        log.info("Notification marked as read: {}", notificationId);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        log.debug("Marking all notifications as read for user: {}", userId);

        Page<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, Pageable.unpaged());

        unreadNotifications.forEach(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
        });

        log.info("All notifications marked as read for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        log.debug("Unread notification count for user {}: {}", userId, count);
        return count;
    }

    // ========== HELPER METHODS ==========

    /**
     * Resolves the title based on notification type.
     */
    private String resolveTitleFromType(String notificationType) {
        return switch (notificationType) {
            case "PAYMENT_SUCCESS" -> "✅ Payment Successful";
            case "PAYMENT_FAILED" -> "❌ Payment Failed";
            case "REFUND" -> "💰 Refund Processed";
            case "ENROLLMENT" -> "🎓 Enrollment Confirmed";
            case "COURSE_REMINDER" -> "📚 Course Reminder";
            default -> "📬 Notification";
        };
    }

    /**
     * Resolves the icon type based on notification type for UI display.
     */
    private String resolveIconType(String notificationType) {
        return switch (notificationType) {
            case "PAYMENT_SUCCESS" -> "check_circle";
            case "PAYMENT_FAILED" -> "error";
            case "REFUND" -> "attach_money";
            case "ENROLLMENT" -> "school";
            case "COURSE_REMINDER" -> "info";
            default -> "notifications";
        };
    }

    /**
     * Maps Notification entity to NotificationResponse DTO.
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getActionUrl(),
                notification.getIconType(),
                notification.getData(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
