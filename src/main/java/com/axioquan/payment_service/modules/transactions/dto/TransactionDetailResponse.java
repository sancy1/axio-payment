package com.axioquan.payment_service.modules.transactions.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for detailed transaction information.
 * Includes all transaction fields with full details.
 */
@JsonPropertyOrder({
    "id",
    "paymentId",
    "userId",
    "transactionType",
    "amount",
    "amountCents",
    "currency",
    "originalCurrency",
    "originalAmountCents",
    "exchangeRate",
    "status",
    "reference",
    "paystackData",
    "createdAt"
})
public record TransactionDetailResponse(
        UUID id,
        UUID paymentId,
        UUID userId,
        String transactionType,
        BigDecimal amount,
        Integer amountCents,
        String currency,
        String originalCurrency,
        Integer originalAmountCents,
        BigDecimal exchangeRate,
        String status,
        String reference,
        String paystackData,
        LocalDateTime createdAt
) {}
