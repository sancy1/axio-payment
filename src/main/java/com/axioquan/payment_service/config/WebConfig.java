// ============================================
// File: WebConfig.java
// Location: /src/main/java/com/axioquan/payment_service/config/WebConfig.java
// ============================================

package com.axioquan.payment_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS is handled in SecurityConfig via CorsConfigurationSource
    // to ensure it works correctly with the Spring Security filter chain.
}