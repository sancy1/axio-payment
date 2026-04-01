package com.axioquan.payment_service.modules.transactions.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new transaction.
 * Validates input and ensures all required fields are present.
 */
@JsonPropertyOrder({
    "userId",
    "paymentId",
    "transactionType",
    "amountCents",
    "currency",
    "originalCurrency",
    "originalAmountCents",
    "exchangeRate",
    "status",
    "reference",
    "paystackData"
})
public record CreateTransactionRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Payment ID is required")
        UUID paymentId,

        @NotBlank(message = "Transaction type is required")
        @Size(min = 1, max = 50, message = "Transaction type must be between 1 and 50 characters")
        String transactionType,

        @NotNull(message = "Amount (in cents) is required")
        @Positive(message = "Amount must be positive")
        Integer amountCents,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency code must be 3 characters (e.g., NGN, USD)")
        String currency,

        @NotBlank(message = "Original currency is required")
        @Size(min = 3, max = 3, message = "Original currency code must be 3 characters")
        String originalCurrency,

        @NotNull(message = "Original amount (in cents) is required")
        @Positive(message = "Original amount must be positive")
        Integer originalAmountCents,

        @Positive(message = "Exchange rate must be positive")
        BigDecimal exchangeRate,

        @NotBlank(message = "Status is required")
        @Size(min = 1, max = 50, message = "Status must be between 1 and 50 characters")
        String status,

        @NotBlank(message = "Reference is required")
        @Size(min = 1, max = 100, message = "Reference must be between 1 and 100 characters")
        String reference,

        String paystackData
) {}
