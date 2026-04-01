// ============================================
// File: HealthController.java
// Location: /src/main/java/com/axioquan/payment_service/modules/health/HealthController.java
// ============================================

package com.axioquan.payment_service.modules.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());

        try {
            jdbcTemplate.execute("SELECT 1");
            response.put("database", "CONNECTED");
        } catch (Exception e) {
            response.put("database", "DOWN: " + e.getMessage());
            return ResponseEntity.status(503).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}