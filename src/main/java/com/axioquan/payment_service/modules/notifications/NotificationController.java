package com.axioquan.payment_service.modules.notifications;

import com.axioquan.payment_service.modules.notifications.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for in-app notification management endpoints.
 * Provides APIs for retrieving and managing notifications in user dashboard.
 */
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for a user with pagination.
     *
     * GET /api/v1/notifications/user/{userId}
     * Query Params: page=0&size=20&sort=createdAt,desc
     *
     * @param userId The user ID
     * @param page   Page number (0-indexed)
     * @param size   Page size (default 20)
     * @return Page of notifications
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Fetching notifications for user: {} [page={}, size={}]", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> response = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notification count for badge display.
     *
     * GET /api/v1/notifications/user/{userId}/unread/count
     *
     * @param userId The user ID
     * @return Count of unread notifications
     */
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@PathVariable UUID userId) {
        log.debug("Fetching unread count for user: {}", userId);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(new UnreadCountResponse(userId, count));
    }

    /**
     * Mark a notification as read.
     *
     * PUT /api/v1/notifications/{id}/read
     *
     * @param id The notification ID
     * @return Success response
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<MarkReadResponse> markAsRead(@PathVariable UUID id) {
        log.debug("Marking notification as read: {}", id);
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new MarkReadResponse(id, true, "Notification marked as read"));
    }

    /**
     * Mark all notifications for a user as read.
     *
     * PUT /api/v1/notifications/user/{userId}/read-all
     *
     * @param userId The user ID
     * @return Success response
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<MarkAllReadResponse> markAllAsRead(@PathVariable UUID userId) {
        log.debug("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(new MarkAllReadResponse(userId, "All notifications marked as read"));
    }

    // ========== RESPONSE DTOs ==========

    /**
     * DTO for unread count response.
     */
    public record UnreadCountResponse(
            UUID userId,
            long unreadCount
    ) {}

    /**
     * DTO for marking single notification as read.
     */
    public record MarkReadResponse(
            UUID notificationId,
            boolean success,
            String message
    ) {}

    /**
     * DTO for marking all notifications as read.
     */
    public record MarkAllReadResponse(
            UUID userId,
            String message
    ) {}
}
