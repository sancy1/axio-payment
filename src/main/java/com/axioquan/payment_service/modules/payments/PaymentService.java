// ============================================
// File 26: PaymentService.java
// Path: src/main/java/com/axioquan/payment_service/modules/payments/PaymentService.java
// ============================================

package com.axioquan.payment_service.modules.payments;

import com.axioquan.payment_service.modules.payments.dto.InitializePaymentRequest;
import com.axioquan.payment_service.modules.payments.dto.InitializePaymentResponse;
import com.axioquan.payment_service.modules.payments.dto.VerifyPaymentResponse;
import com.axioquan.payment_service.modules.webhooks.dto.WebhookPayload;

import java.util.UUID;

public interface PaymentService {

    InitializePaymentResponse initializePayment(InitializePaymentRequest request);

    VerifyPaymentResponse verifyPayment(String reference);

    void processWebhook(WebhookPayload payload);

    VerifyPaymentResponse getPaymentByReference(String reference);

    boolean hasUserPurchasedCourse(UUID userId, UUID courseId);
}