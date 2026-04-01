// ============================================
// File 6: PaystackRefundRequest.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/dto/PaystackRefundRequest.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackRefundRequest {

    @NotBlank
    @JsonProperty("transaction")
    private String transaction; // Paystack transaction reference

    @JsonProperty("amount")
    private Long amount; // Optional (partial refund)

    @Builder.Default
    @JsonProperty("currency")
    private String currency = "NGN";

    @JsonProperty("customer_note")
    private String customerNote;

    @JsonProperty("merchant_note")
    private String merchantNote;
}