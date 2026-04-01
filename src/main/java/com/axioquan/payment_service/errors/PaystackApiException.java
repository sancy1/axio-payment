// ============================================
// File 10: PaystackApiException.java
// Path: src/main/java/com/axioquan/payment_service/errors/PaystackApiException.java
// ============================================

package com.axioquan.payment_service.errors;

import lombok.Getter;

@Getter
public class PaystackApiException extends RuntimeException {

    private final String paystackCode;
    private final String paystackMessage;
    private final int httpStatus;

    public PaystackApiException(String message) {
        super(message);
        this.paystackCode = null;
        this.paystackMessage = message;
        this.httpStatus = 500;
    }

    public PaystackApiException(String message, Throwable cause) {
        super(message, cause);
        this.paystackCode = null;
        this.paystackMessage = message;
        this.httpStatus = 500;
    }

    public PaystackApiException(String message, String paystackCode, String paystackMessage, int httpStatus) {
        super(message);
        this.paystackCode = paystackCode;
        this.paystackMessage = paystackMessage;
        this.httpStatus = httpStatus;
    }
}