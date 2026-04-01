// ============================================
// File 4: PaystackVerifyResponse.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/dto/PaystackVerifyResponse.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackVerifyResponse {

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private VerificationData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerificationData {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("status")
        private String status;

        @JsonProperty("amount")
        private Long amount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("channel")
        private String channel;

        @JsonProperty("fees")
        private Long fees;

        @JsonProperty("gateway_response")
        private String gatewayResponse;

        @JsonProperty("ip_address")
        private String ipAddress;

        @JsonProperty("paid_at")
        private OffsetDateTime paidAt;

        @JsonProperty("created_at")
        private OffsetDateTime createdAt;

        @JsonProperty("customer")
        private Customer customer;

        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Customer {

            @JsonProperty("id")
            private Long id;

            @JsonProperty("email")
            private String email;

            @JsonProperty("customer_code")
            private String customerCode;
        }
    }
}