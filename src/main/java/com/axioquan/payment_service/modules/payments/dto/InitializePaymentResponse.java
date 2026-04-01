// ============================================
// File 29: InitializePaymentResponse.java
// Path: src/main/java/com/axioquan/payment_service/modules/payments/dto/InitializePaymentResponse.java
// ============================================

package com.axioquan.payment_service.modules.payments.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitializePaymentResponse {

    private String reference;
    private String authorizationUrl;
    private String accessCode;
}