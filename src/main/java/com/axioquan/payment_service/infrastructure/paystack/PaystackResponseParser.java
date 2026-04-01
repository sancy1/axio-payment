// ============================================
// File 15: PaystackResponseParser.java
// Path: src/main/java/com/axioquan/payment_service/infrastructure/paystack/PaystackResponseParser.java
// ============================================

package com.axioquan.payment_service.infrastructure.paystack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.axioquan.payment_service.infrastructure.paystack.dto.PaystackVerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaystackResponseParser {
    
    private final ObjectMapper objectMapper;
    
    public PaystackResponseParser() {
        this.objectMapper = new ObjectMapper();
    }
    
    public String extractMetadataValue(PaystackVerifyResponse response, String key) {
        if (response == null || response.getData() == null || response.getData().getMetadata() == null) {
            return null;
        }
        Object value = response.getData().getMetadata().get(key);
        return value != null ? value.toString() : null;
    }
    
    public JsonNode parseWebhookPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse webhook payload", e);
            return null;
        }
    }
    
    public String extractTransactionReference(JsonNode payload) {
        if (payload == null) return null;
        JsonNode data = payload.get("data");
        if (data == null) return null;
        JsonNode reference = data.get("reference");
        return reference != null ? reference.asText() : null;
    }
    
    public String extractEventType(JsonNode payload) {
        if (payload == null) return null;
        JsonNode event = payload.get("event");
        return event != null ? event.asText() : null;
    }
}