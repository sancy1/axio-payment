
// ============================================
// File : CurrencyConverter.java
// Path: src/main/java/com/axioquan/payment_service/utils/CurrencyConverter.java
// ============================================

package com.axioquan.payment_service.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CurrencyConverter {

    /**
     * Convert amount from one currency to another
     * NOTE: Replace hardcoded rates with real API later
     */
    public BigDecimal convert(BigDecimal amount, String from, String to) {

        if (from.equalsIgnoreCase(to)) {
            return amount;
        }

        // Example conversion rates (TEMP)
        if (from.equalsIgnoreCase("USD") && to.equalsIgnoreCase("NGN")) {
            return amount.multiply(BigDecimal.valueOf(1500)); // $1 = ₦1500
        }

        if (from.equalsIgnoreCase("NGN") && to.equalsIgnoreCase("USD")) {
            return amount.divide(BigDecimal.valueOf(1500));
        }

        throw new RuntimeException("Unsupported currency conversion: " + from + " -> " + to);
    }
}