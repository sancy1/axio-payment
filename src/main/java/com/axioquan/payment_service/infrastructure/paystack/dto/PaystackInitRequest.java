// ============================================
// File 2: PaystackInitRequest.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/dto/PaystackInitRequest.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaystackInitRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}