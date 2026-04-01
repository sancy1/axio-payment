package com.axioquan.payment_service.modules.transactions.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for transaction list/summary information.
 * Includes essential transaction fields for list views.
 */
@JsonPropertyOrder({
    "id",
    "transactionType",
    "amount",
    "currency",
    "status",
    "reference",
    "createdAt"
})
public record TransactionListResponse(
        UUID id,
        String transactionType,
        BigDecimal amount,
        String currency,
        String status,
        String reference,
        LocalDateTime createdAt
) {}
