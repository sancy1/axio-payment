// ============================================
// File 3: PaystackInitResponse.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/dto/PaystackInitResponse.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackInitResponse {

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private InitData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitData {

        @JsonProperty("authorization_url")
        private String authorizationUrl;

        @JsonProperty("access_code")
        private String accessCode;

        @JsonProperty("reference")
        private String reference;
    }
}