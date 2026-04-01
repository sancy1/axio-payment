// ============================================
// File 19: Transaction.java
// Path: src/main/java/com/axioquan/payment_service/domain/entities/Transaction.java
// ============================================

package com.axioquan.payment_service.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID paymentId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String transactionType;

    @Column(nullable = false)
    private Integer amountCents;

    @Column(length = 3)
    private String currency;

    @Column(length = 3)
    private String originalCurrency;

    private Integer originalAmountCents;

    @Column(precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @Column(length = 50)
    private String status;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String paystackData;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public BigDecimal getAmount() {
        return BigDecimal.valueOf(amountCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}