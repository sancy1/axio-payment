// ============================================
// File 22: PaymentRepository.java
// Path: src/main/java/com/axioquan/payment_service/modules/payments/PaymentRepository.java
// ============================================

package com.axioquan.payment_service.modules.payments;

import com.axioquan.payment_service.domain.entities.Payment;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByReference(String reference);

    List<Payment> findByUserIdAndStatus(UUID userId, String status);

    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    List<Payment> findByStatusOrderByCreatedAtAsc(String status);

    Optional<Payment> findByPaystackReference(String paystackReference);

    boolean existsByUserIdAndCourseIdAndStatus(UUID userId, UUID courseId, String status);

    List<Payment> findByUserIdAndCourseIdAndStatusOrderByCreatedAtDesc(UUID userId, UUID courseId, String status);

    @Query("""
        SELECT p FROM Payment p 
        WHERE p.status = 'SUCCESS' 
        AND p.paidAt BETWEEN :startDate AND :endDate
    """)
    List<Payment> findSuccessfulPaymentsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(p.amountCents), 0) 
        FROM Payment p 
        WHERE p.status = 'SUCCESS' 
        AND p.userId = :userId
    """)
    Long getTotalSpentByUser(@Param("userId") UUID userId);

    @Query("""
        SELECT p FROM Payment p 
        WHERE p.status = 'SUCCESS' 
        AND p.notificationSent = false
    """)
    List<Payment> findSuccessfulPaymentsWithPendingNotifications();

    @Query("""
        SELECT COUNT(p) FROM Payment p 
        WHERE p.status = 'SUCCESS' 
        AND p.paidAt >= :since
    """)
    long countSuccessfulPaymentsSince(@Param("since") LocalDateTime since);

    @Query("""
        SELECT COALESCE(SUM(p.amountCents), 0) 
        FROM Payment p 
        WHERE p.status = 'SUCCESS'
    """)
    Long getTotalRevenue();

    @Query("""
        SELECT p.originalCurrency, COUNT(p), SUM(p.originalAmountCents)
        FROM Payment p 
        WHERE p.status = 'SUCCESS'
        GROUP BY p.originalCurrency
    """)
    List<Object[]> getRevenueByCurrency();

    @Modifying
    @Transactional
    @Query("""
        UPDATE Payment p 
        SET p.notificationSent = true, 
            p.notificationSentAt = :sentAt,
            p.notificationStatus = 'sent'
        WHERE p.id = :paymentId
    """)
    int markNotificationSent(
            @Param("paymentId") UUID paymentId,
            @Param("sentAt") LocalDateTime sentAt
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE Payment p 
        SET p.notificationStatus = 'failed',
            p.notificationError = :error
        WHERE p.id = :paymentId
    """)
    int markNotificationFailed(
            @Param("paymentId") UUID paymentId,
            @Param("error") String error
    );
}