// ============================================
// File 5: PaystackBankResponse.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/dto/PaystackBankResponse.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackBankResponse {

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<BankData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankData {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("slug")
        private String slug;

        @JsonProperty("code")
        private String code;

        @JsonProperty("country")
        private String country;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("type")
        private String type;
    }
}