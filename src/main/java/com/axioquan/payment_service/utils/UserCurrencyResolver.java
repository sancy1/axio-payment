


// ============================================
// File : UserCurrencyResolver.java
// Path: src/main/java/com/axioquan/payment_service/utils/UserCurrencyResolver.java
// ============================================

package com.axioquan.payment_service.utils;

import org.springframework.stereotype.Component;

@Component
public class UserCurrencyResolver {

    /**
     * Resolve user's currency based on email / region / future logic
     * For now: default to NGN
     */
    public String resolve(String email) {

        // TODO: Replace with real logic (GeoIP / user profile / frontend input)

        return "NGN"; // Default for now
    }
}