// ============================================
// File 18: Payment.java
// Path: src/main/java/com/axioquan/payment_service/domain/entities/Payment.java
// ============================================

package com.axioquan.payment_service.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Column(length = 3)
    private String currency;

    @Column(name = "original_currency", nullable = false, length = 3)
    private String originalCurrency;

    @Column(name = "original_amount_cents", nullable = false)
    private Integer originalAmountCents;

    @Column(precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "settlement_currency", length = 3)
    private String settlementCurrency;

    @Column(length = 50)
    private String status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "paystack_reference", length = 100)
    private String paystackReference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String paystackResponse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    private Boolean notificationSent = false;

    private LocalDateTime notificationSentAt;

    @Column(length = 50)
    private String notificationStatus;

    private String notificationError;

    // ================= BUSINESS METHODS =================

    public BigDecimal getAmountNgn() {
        return BigDecimal.valueOf(amountCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getOriginalAmount() {
        return BigDecimal.valueOf(originalAmountCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }

    public void markAsSuccess() {
        this.status = "SUCCESS";
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = "FAILED";
    }

    public void markNotificationSent() {
        this.notificationSent = true;
        this.notificationSentAt = LocalDateTime.now();
        this.notificationStatus = "sent";
    }

    public void markNotificationFailed(String error) {
        this.notificationStatus = "failed";
        this.notificationError = error;
    }
}