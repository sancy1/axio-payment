// ============================================
// File 28: InitializePaymentRequest.java
// Path: src/main/java/com/axioquan/payment_service/modules/payments/dto/InitializePaymentRequest.java
// ============================================

package com.axioquan.payment_service.modules.payments.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InitializePaymentRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    /**
     * Price in kobo (NGN) or smallest currency unit.
     * Passed by the Next.js server — avoids DB lookup in payment service.
     * If null or 0, the course is treated as free.
     */
    @Min(value = 0, message = "Amount cannot be negative")
    private Integer amountCents;

    private String courseName;
}