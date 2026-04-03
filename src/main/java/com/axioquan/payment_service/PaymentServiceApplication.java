// ============================================
// File: PaymentServiceApplication.java
// Location: /src/main/java/com/axioquan/payment_service/PaymentServiceApplication.java
// ============================================

package com.axioquan.payment_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.URI;

@SpringBootApplication
@EnableAsync  // Enable async execution for @Async annotated methods
public class PaymentServiceApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        // 🔥 FIX DATABASE_URL → JDBC FORMAT
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(databaseUrl);

                String username = uri.getUserInfo().split(":")[0];
                String password = uri.getUserInfo().split(":")[1];

                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();

                if (uri.getQuery() != null) {
                    jdbcUrl += "?" + uri.getQuery();
                }

                System.setProperty("spring.datasource.url", jdbcUrl);
                System.setProperty("spring.datasource.username", username);
                System.setProperty("spring.datasource.password", password);

            } catch (Exception e) {
                throw new RuntimeException("Invalid DATABASE_URL format", e);
            }
        }

        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}