// ============================================
// File 32: PaymentAlreadyProcessedException.java
// Path: src/main/java/com/axioquan/payment_service/errors/PaymentAlreadyProcessedException.java
// ============================================

package com.axioquan.payment_service.errors;

public class PaymentAlreadyProcessedException extends RuntimeException {

    public PaymentAlreadyProcessedException(String message) {
        super(message);
    }

    public PaymentAlreadyProcessedException(String message, Throwable cause) {
        super(message, cause);
    }
}