// ============================================
// File 8: PaystackErrorResponse.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/dto/PaystackErrorResponse.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackErrorResponse {

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("type")
    private String type;

    @JsonProperty("code")
    private String code;
}