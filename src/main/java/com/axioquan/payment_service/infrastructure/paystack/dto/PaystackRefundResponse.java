// ============================================
// File 7: PaystackRefundResponse.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/dto/PaystackRefundResponse.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackRefundResponse {

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private RefundData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RefundData {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("transaction")
        private Long transaction;

        @JsonProperty("amount")
        private Long amount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("status")
        private String status;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("createdAt") // Paystack uses camelCase here
        private OffsetDateTime createdAt;
    }
}