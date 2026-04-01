// ============================================
// File 30: VerifyPaymentResponse.java
// Path: src/main/java/com/axioquan/payment_service/modules/payments/dto/VerifyPaymentResponse.java
// ============================================

package com.axioquan.payment_service.modules.payments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyPaymentResponse {

    private String reference;
    private String status;
    private BigDecimal amountPaid;
    private String currency;
    private LocalDateTime paidAt;
    // ✅ NEW: Enrollment details
    private UUID enrollmentId;
    private UUID userId;
    private UUID courseId;
    private String enrollmentStatus;
    private Boolean hasEnrollment;
    private Boolean isPaid;
}