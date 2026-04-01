// ============================================
// File 25: PaymentNotificationRepository.java
// Path: src/main/java/com/axioquan/payment_service/modules/notifications/PaymentNotificationRepository.java
// ============================================

package com.axioquan.payment_service.modules.notifications;

import com.axioquan.payment_service.domain.entities.PaymentNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // ✅ FIXED
import java.util.UUID;

public interface PaymentNotificationRepository extends JpaRepository<PaymentNotification, UUID> {

    Page<PaymentNotification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<PaymentNotification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadAtIsNull(UUID userId);

    Optional<PaymentNotification> findByPaymentId(UUID paymentId);

    List<PaymentNotification> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentNotification pn SET pn.readAt = :readAt, pn.status = 'read' " +
           "WHERE pn.id = :id AND pn.userId = :userId")
    int markAsRead(@Param("id") UUID id,
                   @Param("userId") UUID userId,
                   @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentNotification pn SET pn.readAt = :readAt, pn.status = 'read' " +
           "WHERE pn.userId = :userId AND pn.readAt IS NULL")
    int markAllAsRead(@Param("userId") UUID userId,
                      @Param("readAt") LocalDateTime readAt);
}