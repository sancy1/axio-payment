
// ============================================
// File 1: PaystackProperties.java
// Path: src/main/java/com/axioquan/payment_service/config/PaystackProperties.java
// ============================================

package com.axioquan.payment_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paystack")
@Getter
@Setter
public class PaystackProperties {

    // MUST match application.yml: paystack.secret-key
    private String secretKey;

    // MUST match application.yml: paystack.public-key
    private String publicKey;

    private String baseUrl;
    private String callbackUrl;
    private String webhookUrl;
    private String webhookSecret;

    private int connectionTimeout;
    private int readTimeout;
    private int retryCount;
    private long retryDelayMs;
}