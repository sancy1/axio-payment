// ============================================
// File: Enrollment.java
// Path: src/main/java/com/axioquan/payment_service/domain/entities/Enrollment.java
// ============================================

package com.axioquan.payment_service.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    @Column(name = "enrolled_price_cents")
    private Integer enrolledPriceCents;

    @Column(name = "access_type", columnDefinition = "varchar default 'full'")
    private String accessType;

    @Column(name = "enrollment_source")
    private String enrollmentSource;

    @Column(name = "referral_code")
    private String referralCode;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "current_lesson_id")
    private UUID currentLessonId;

    @Column(name = "current_module_id")
    private UUID currentModuleId;

    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage;

    @Column(name = "completed_lessons")
    private Integer completedLessons;

    @Column(name = "total_lessons")
    private Integer totalLessons;

    @Column(name = "total_time_spent")
    private Integer totalTimeSpent;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "average_quiz_score", precision = 5, scale = 2)
    private BigDecimal averageQuizScore;

    @Column(name = "assignment_average", precision = 5, scale = 2)
    private BigDecimal assignmentAverage;

    @Column(name = "overall_grade", precision = 5, scale = 2)
    private BigDecimal overallGrade;

    @Column(name = "status", columnDefinition = "varchar default 'active'")
    private String status;

    @Column(name = "can_unenroll", columnDefinition = "boolean default true")
    private Boolean canUnenroll;

    @Column(name = "original_currency", length = 3)
    private String originalCurrency;

    @Column(name = "original_amount_cents")
    private Integer originalAmountCents;

    // ================= BUSINESS METHODS =================

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public boolean isPaid() {
        return enrolledPriceCents != null && enrolledPriceCents > 0;
    }

    public boolean canUnenrollFromCourse() {
        return canUnenroll != null && canUnenroll;
    }
}
