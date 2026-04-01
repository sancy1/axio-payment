// ============================================
// File: EnrollmentServiceImpl.java
// Path: src/main/java/com/axioquan/payment_service/modules/enrollments/EnrollmentServiceImpl.java
// ============================================

package com.axioquan.payment_service.modules.enrollments;

import com.axioquan.payment_service.domain.entities.Enrollment;
import com.axioquan.payment_service.modules.enrollments.dto.EnrollmentResponse;
import com.axioquan.payment_service.modules.enrollments.dto.UpdateProgressRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Optional<EnrollmentResponse> getEnrollment(UUID userId, UUID courseId) {
        log.info("Getting enrollment for userId: {}, courseId: {}", userId, courseId);
        Optional<Enrollment> enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        log.info("Enrollment found: {}", enrollment.isPresent());
        if (enrollment.isPresent()) {
            log.debug("Enrollment details - id: {}, status: {}, canUnenroll: {}", 
                enrollment.get().getId(), enrollment.get().getStatus(), enrollment.get().getCanUnenroll());
        }
        return enrollment.map(this::mapToResponse);
    }

    @Override
    public boolean hasAccess(UUID userId, UUID courseId) {
        return enrollmentRepository.existsActiveEnrollment(userId, courseId);
    }

    @Override
    public boolean hasPaidAccess(UUID userId, UUID courseId) {
        return enrollmentRepository.hasPaidEnrollment(userId, courseId);
    }

    @Override
    public List<EnrollmentResponse> getUserEnrollments(UUID userId) {
        return enrollmentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<EnrollmentResponse> getCourseEnrollments(UUID courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Optional<EnrollmentResponse> getEnrollmentByPayment(UUID paymentId) {
        return enrollmentRepository.findByPaymentId(paymentId)
                .map(this::mapToResponse);
    }

    /**
     * Map Enrollment entity to response DTO
     */
    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .paymentId(enrollment.getPaymentId())
                .enrolledAt(enrollment.getEnrolledAt())
                .enrolledPriceCents(enrollment.getEnrolledPriceCents())
                .accessType(enrollment.getAccessType())
                .enrollmentSource(enrollment.getEnrollmentSource())
                .status(enrollment.getStatus())
                .canUnenroll(enrollment.getCanUnenroll())
                .lastAccessedAt(enrollment.getLastAccessedAt())
                .completedAt(enrollment.getCompletedAt())
                .progressPercentage(enrollment.getProgressPercentage())
                .completedLessons(enrollment.getCompletedLessons())
                .totalLessons(enrollment.getTotalLessons())
                .totalTimeSpent(enrollment.getTotalTimeSpent())
                .averageQuizScore(enrollment.getAverageQuizScore())
                .isPaid(enrollment.isPaid())
                .isActive(enrollment.isActive())
                .build();
    }

    // ============================================
    // PROGRESS TRACKING & COMPLETION (NEW)
    // ============================================

    @Override
    @Transactional
    public void updateProgress(UUID enrollmentId, UpdateProgressRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Update progress fields
        if (request.getProgressPercentage() != null) {
            enrollment.setProgressPercentage(request.getProgressPercentage());
        }
        if (request.getCompletedLessons() != null) {
            enrollment.setCompletedLessons(request.getCompletedLessons());
        }
        if (request.getTotalLessons() != null) {
            enrollment.setTotalLessons(request.getTotalLessons());
        }
        if (request.getTimeSpentMinutes() != null) {
            enrollment.setTotalTimeSpent(request.getTimeSpentMinutes());
        }
        if (request.getAverageQuizScore() != null) {
            enrollment.setAverageQuizScore(request.getAverageQuizScore());
        }
        if (request.getAssignmentAverage() != null) {
            enrollment.setAssignmentAverage(request.getAssignmentAverage());
        }
        if (request.getOverallGrade() != null) {
            enrollment.setOverallGrade(request.getOverallGrade());
        }

        // Update last activity
        enrollment.setLastActivityAt(LocalDateTime.now());
        enrollment.setLastAccessedAt(LocalDateTime.now());

        // Mark as completed if requested
        if (request.getMarkCompleted() != null && request.getMarkCompleted()) {
            enrollment.setCompletedAt(LocalDateTime.now());
            enrollment.setStatus("completed");
            enrollment.setProgressPercentage(BigDecimal.valueOf(100));
        }

        enrollmentRepository.save(enrollment);
        log.info("Updated progress for enrollment: {}", enrollmentId);
    }

    @Override
    public BigDecimal getProgress(UUID enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .map(Enrollment::getProgressPercentage)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public void markAsCompleted(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setCompletedAt(LocalDateTime.now());
        enrollment.setStatus("completed");
        enrollment.setProgressPercentage(BigDecimal.valueOf(100));

        enrollmentRepository.save(enrollment);
        log.info("Marked enrollment as completed: {}", enrollmentId);
    }

    @Override
    public boolean isCompleted(UUID enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .map(Enrollment::isCompleted)
                .orElse(false);
    }

    @Override
    public List<EnrollmentResponse> getUserEnrollmentsWithProgress(UUID userId) {
        return enrollmentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ============================================
    // PAID COURSE FOCUSED: UNENROLLMENT RESTRICTIONS
    // ============================================

    @Override
    public boolean canUnenroll(UUID userId, UUID courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> {
                    // Paid courses (can_unenroll=false) cannot be unenrolled
                    // Free courses (can_unenroll=true) can be unenrolled
                    Boolean canUnenroll = enrollment.getCanUnenroll();
                    return canUnenroll != null && canUnenroll;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void unenroll(UUID userId, UUID courseId) throws IllegalAccessException {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // CRITICAL: Prevent unenrollment from paid courses
        if (enrollment.isPaid() && !canUnenroll(userId, courseId)) {
            throw new IllegalAccessException(
                    "Cannot unenroll from paid course. You have lifetime access."
            );
        }

        enrollment.setStatus("unenrolled");
        enrollmentRepository.save(enrollment);
        log.info("User {} unenrolled from course {}", userId, courseId);
    }

    @Override
    public String getUnenrollBlockReason(UUID userId, UUID courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> {
                    if (enrollment.isPaid() && !canUnenroll(userId, courseId)) {
                        return "This is a paid course with lifetime access. " +
                                "You cannot unenroll but can pause your learning.";
                    }
                    return null;
                })
                .orElse("Enrollment not found");
    }

    // ============================================
    // PAID COURSE SPECIFIC LOGIC
    // ============================================

    @Override
    public boolean isCoursePaid(UUID courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .anyMatch(Enrollment::isPaid);
    }

    @Override
    public long countPaidEnrollments(UUID userId) {
        return enrollmentRepository.countPaidEnrollments(userId);
    }

    @Override
    public long getTotalSpentCents(UUID userId) {
        return enrollmentRepository.findByUserId(userId)
                .stream()
                .filter(Enrollment::isPaid)
                .mapToLong(e -> e.getEnrolledPriceCents() != null ? e.getEnrolledPriceCents() : 0)
                .sum();
    }

    @Override
    public boolean hasLifetimeAccess(UUID userId, UUID courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(enrollment -> {
                    // Lifetime access = Paid course + can_unenroll = false
                    return enrollment.isPaid() && 
                           (enrollment.getCanUnenroll() == null || !enrollment.getCanUnenroll());
                })
                .orElse(false);
    }

    // ============================================
    // NEW: COURSE SEPARATION (Free vs Paid)
    // ============================================

    @Override
    public List<EnrollmentResponse> getUserPaidCourses(UUID userId) {
        return enrollmentRepository.findByUserId(userId)
                .stream()
                .filter(Enrollment::isPaid)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<EnrollmentResponse> getUserFreeCourses(UUID userId) {
        return enrollmentRepository.findByUserId(userId)
                .stream()
                .filter(e -> !e.isPaid())
                .map(this::mapToResponse)
                .toList();
    }
}
