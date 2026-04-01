// ============================================
// File 24: WebhookLogRepository.java
// Path: src/main/java/com/axioquan/payment_service/modules/webhooks/WebhookLogRepository.java
// ============================================

package com.axioquan.payment_service.modules.webhooks;

import com.axioquan.payment_service.domain.entities.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, UUID> {

    List<WebhookLog> findByProcessedFalseOrderByCreatedAtAsc();

    List<WebhookLog> findByEventType(String eventType);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM webhook_logs WHERE event_type = :eventType AND payload::text LIKE CONCAT('%', :reference, '%')", nativeQuery = true)
    boolean existsByEventTypeAndPayloadContaining(@Param("eventType") String eventType, @Param("reference") String reference);
}