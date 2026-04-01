// ============================================
// File 14: PaystackWebhookVerifier.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/PaystackWebhookVerifier.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack;

import com.axioquan.payment_service.config.PaystackProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
@Slf4j
public class PaystackWebhookVerifier {
    
    private final PaystackProperties paystackProperties;
    
    public PaystackWebhookVerifier(PaystackProperties paystackProperties) {
        this.paystackProperties = paystackProperties;
    }
    
    /**
     * Verify the signature of a Paystack webhook request
     */
    public boolean verifySignature(String payload, String signature) {
        if (payload == null || signature == null) {
            log.warn("Missing payload or signature");
            return false;
        }
        
        String webhookSecret = paystackProperties.getWebhookSecret();
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            log.warn("Webhook secret not configured");
            return false;
        }
        
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                webhookSecret.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA512"
            );
            mac.init(secretKeySpec);
            
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder expectedSignature = new StringBuilder();
            for (byte b : hmacBytes) {
                expectedSignature.append(String.format("%02x", b));
            }
            
            boolean isValid = expectedSignature.toString().equals(signature);
            
            if (!isValid) {
                log.warn("Invalid webhook signature");
            }
            
            return isValid;
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Webhook verification failed", e);
            return false;
        }
    }
}