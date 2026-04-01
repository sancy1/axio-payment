// ============================================
// File 11: PaystackRateLimitException.java
// Path: src/main/java/com/axioquan/payment_service/errors/PaystackRateLimitException.java
// ============================================

package com.axioquan.payment_service.errors;

import lombok.Getter;

@Getter
public class PaystackRateLimitException extends PaystackApiException {

    private final int retryAfterSeconds;

    public PaystackRateLimitException(String message, int retryAfterSeconds) {
        super(message, "RATE_LIMIT", message, 429);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}