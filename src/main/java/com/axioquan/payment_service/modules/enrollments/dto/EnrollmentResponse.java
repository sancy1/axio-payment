// ============================================
// File: EnrollmentResponse.java
// Path: src/main/java/com/axioquan/payment_service/modules/enrollments/dto/EnrollmentResponse.java
// ============================================

package com.axioquan.payment_service.modules.enrollments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnrollmentResponse {

    private UUID id;
    private UUID userId;
    private UUID courseId;
    private UUID paymentId;
    private LocalDateTime enrolledAt;
    private Integer enrolledPriceCents;
    private String accessType;
    private String enrollmentSource;
    private String status;
    private Boolean canUnenroll;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime completedAt;
    private BigDecimal progressPercentage;
    private Integer completedLessons;
    private Integer totalLessons;
    private Integer totalTimeSpent;
    private BigDecimal averageQuizScore;
    private Boolean isPaid;
    private Boolean isActive;
}
