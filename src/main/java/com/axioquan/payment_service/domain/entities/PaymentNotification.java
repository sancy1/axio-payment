// ============================================
// File 21: PaymentNotification.java
// Path: src/main/java/com/axioquan/payment_service/domain/entities/PaymentNotification.java
// ============================================

package com.axioquan.payment_service.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID courseId;

    @Column(nullable = false, length = 50)
    private String notificationType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(length = 50)
    private String status;

    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
        this.status = "read";
    }

    public void markAsSent() {
        this.sentAt = LocalDateTime.now();
        this.status = "sent";
    }
}