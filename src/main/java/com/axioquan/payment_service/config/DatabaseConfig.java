
// ============================================
// File: DatabaseConfig.java
// Location: /src/main/java/com/axioquan/payment_service/config/DatabaseConfig.java
// ============================================

package com.axioquan.payment_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.axioquan.payment_service.modules")
public class DatabaseConfig {
    // Spring Boot handles everything automatically
}