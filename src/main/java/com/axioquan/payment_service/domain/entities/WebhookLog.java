// ============================================
// File 20: WebhookLog.java
// Path: src/main/java/com/axioquan/payment_service/domain/entities/WebhookLog.java
// ============================================

package com.axioquan.payment_service.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "webhook_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(length = 255)
    private String signature;

    @Builder.Default
    private Boolean processed = false;

    private LocalDateTime processedAt;

    private String errorMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void markProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed(String error) {
        this.processed = false;
        this.errorMessage = error;
    }
}