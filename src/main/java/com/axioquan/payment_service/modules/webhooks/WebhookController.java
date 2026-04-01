// ============================================
// File 34: WebhookController.java
// Path: src/main/java/com/axioquan/payment_service/modules/webhooks/WebhookController.java
// ============================================

package com.axioquan.payment_service.modules.webhooks;

import com.axioquan.payment_service.infrastructure.paystack.PaystackWebhookVerifier;
import com.axioquan.payment_service.modules.payments.PaymentService;
import com.axioquan.payment_service.modules.webhooks.dto.WebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;
    private final PaystackWebhookVerifier webhookVerifier;
    private final ObjectMapper objectMapper;

    // ============================================
    // PAYSTACK WEBHOOK ENDPOINT
    // ============================================

    @PostMapping("/paystack")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestHeader(value = "x-paystack-signature", required = false) String signature,
            @RequestBody String payload) {

        log.info("Received Paystack webhook");

        // ============================================
        // 1. VERIFY SIGNATURE
        // ============================================

        if (!webhookVerifier.verifySignature(payload, signature)) {
            log.warn("Invalid Paystack webhook signature");
            return ResponseEntity.status(401).body("Invalid signature");
        }

        log.info("Webhook signature verified");

        // ============================================
        // 2. PARSE + PROCESS
        // ============================================

        try {
            WebhookPayload webhookPayload =
                    objectMapper.readValue(payload, WebhookPayload.class);

            log.info("Processing webhook event: {}", webhookPayload.getEvent());

            paymentService.processWebhook(webhookPayload);

            // ============================================
            // 3. ACKNOWLEDGE (IMPORTANT)
            // ============================================

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {

            log.error("Webhook processing failed", e);

            // IMPORTANT: Always return 200 so Paystack doesn't retry endlessly
            return ResponseEntity.ok("Webhook received but error logged");
        }
    }
}