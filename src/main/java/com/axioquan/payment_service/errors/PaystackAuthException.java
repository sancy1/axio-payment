// ============================================
// File 12: PaystackAuthException.java
// Path: src/main/java/com/axioquan/payment_service/errors/PaystackAuthException.java
// ============================================

package com.axioquan.payment_service.errors;

public class PaystackAuthException extends PaystackApiException {

    public PaystackAuthException(String message) {
        super(message, "AUTH_ERROR", message, 401);
    }
}