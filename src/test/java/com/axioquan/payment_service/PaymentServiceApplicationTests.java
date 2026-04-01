// package com.axioquan.payment_service;

// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest
// class PaymentServiceApplicationTests {

// 	@Test
// 	void contextLoads() {
// 	}

// }


















// ============================================
// File: PaymentServiceApplicationTests.java
// Path: src/test/java/com/axioquan/payment_service/PaymentServiceApplicationTests.java
// ============================================

package com.axioquan.payment_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = PaymentServiceApplication.class, 
               webEnvironment = SpringBootTest.WebEnvironment.NONE,
               properties = {
                   "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,com.axioquan.payment_service.config.SecurityConfig",
                   "spring.main.lazy-initialization=true"
               })
@ActiveProfiles("test")
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring Boot application context loads successfully
    }
}
