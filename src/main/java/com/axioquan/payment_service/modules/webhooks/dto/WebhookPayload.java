// ============================================
// File 31: WebhookPayload.java
// Path: src/main/java/com/axioquan/payment_service/modules/webhooks/dto/WebhookPayload.java
// ============================================

package com.axioquan.payment_service.modules.webhooks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // ✅ CRITICAL FIX
public class WebhookPayload {

    @JsonProperty("event")
    private String event;

    @JsonProperty("data")
    private WebhookData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // ✅ FIX
    public static class WebhookData {

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("amount")
        private Long amount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("status")
        private String status;

        @JsonProperty("channel")
        private String channel;

        @JsonProperty("customer")
        private Customer customer;

        @JsonProperty("metadata")
        private Object metadata;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // ✅ FIX
    public static class Customer {

        @JsonProperty("email")
        private String email;

        @JsonProperty("customer_code")
        private String customerCode;
    }
}