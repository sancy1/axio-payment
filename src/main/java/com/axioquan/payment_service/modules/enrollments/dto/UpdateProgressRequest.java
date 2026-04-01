// ============================================
// File: UpdateProgressRequest.java
// Path: src/main/java/com/axioquan/payment_service/modules/enrollments/dto/UpdateProgressRequest.java
// ============================================

package com.axioquan.payment_service.modules.enrollments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProgressRequest {

    /**
     * Percentage of course completed (0-100)
     */
    private BigDecimal progressPercentage;

    /**
     * Number of lessons completed
     */
    private Integer completedLessons;

    /**
     * Total lessons in course
     */
    private Integer totalLessons;

    /**
     * Time spent in milliseconds or seconds
     */
    private Integer timeSpentMinutes;

    /**
     * Quiz score (0-100)
     */
    private BigDecimal averageQuizScore;

    /**
     * Assignment score (0-100)
     */
    private BigDecimal assignmentAverage;

    /**
     * Overall grade (0-100)
     */
    private BigDecimal overallGrade;

    /**
     * Current module being studied
     */
    private String currentModuleId;

    /**
     * Current lesson being studied
     */
    private String currentLessonId;

    /**
     * Whether to mark as completed
     */
    private Boolean markCompleted;
}
