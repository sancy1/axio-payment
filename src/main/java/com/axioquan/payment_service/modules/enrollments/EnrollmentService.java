// ============================================
// File: EnrollmentService.java
// Path: src/main/java/com/axioquan/payment_service/modules/enrollments/EnrollmentService.java
// ============================================

package com.axioquan.payment_service.modules.enrollments;

import com.axioquan.payment_service.modules.enrollments.dto.EnrollmentResponse;
import com.axioquan.payment_service.modules.enrollments.dto.UpdateProgressRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentService {

    // ============================================
    // CORE ENROLLMENT QUERIES
    // ============================================

    /**
     * Get enrollment by user and course
     */
    Optional<EnrollmentResponse> getEnrollment(UUID userId, UUID courseId);

    /**
     * Check if user has access to course
     */
    boolean hasAccess(UUID userId, UUID courseId);

    /**
     * Check if user has paid for access (CRITICAL for paid courses)
     */
    boolean hasPaidAccess(UUID userId, UUID courseId);

    /**
     * Get enrollment by payment ID (after successful payment)
     */
    Optional<EnrollmentResponse> getEnrollmentByPayment(UUID paymentId);

    // ============================================
    // USER COURSE LISTS
    // ============================================

    /**
     * Get all user enrollments (free + paid)
     */
    List<EnrollmentResponse> getUserEnrollments(UUID userId);

    /**
     * Get only PAID courses user has purchased
     */
    List<EnrollmentResponse> getUserPaidCourses(UUID userId);

    /**
     * Get only FREE courses user is enrolled in
     */
    List<EnrollmentResponse> getUserFreeCourses(UUID userId);

    /**
     * Get all course enrollments (admin only)
     */
    List<EnrollmentResponse> getCourseEnrollments(UUID courseId);

    // ============================================
    // PROGRESS TRACKING & COMPLETION
    // ============================================

    /**
     * Update enrollment progress (for paid courses)
     * Tracks: lessons completed, time spent, quiz scores
     */
    void updateProgress(UUID enrollmentId, UpdateProgressRequest request);

    /**
     * Get enrollment progress percentage
     */
    BigDecimal getProgress(UUID enrollmentId);

    /**
     * Mark enrollment as completed
     */
    void markAsCompleted(UUID enrollmentId);

    /**
     * Check if enrollment is completed
     */
    boolean isCompleted(UUID enrollmentId);

    /**
     * Get completion percentage for all user courses
     */
    List<EnrollmentResponse> getUserEnrollmentsWithProgress(UUID userId);

    // ============================================
    // UNENROLLMENT (Restricted for Paid Courses)
    // ============================================

    /**
     * Check if user can unenroll from course
     * Returns false for paid courses (can_unenroll = false)
     * Returns true for free courses (can_unenroll = true)
     */
    boolean canUnenroll(UUID userId, UUID courseId);

    /**
     * Unenroll user from course (only if allowed)
     * Throws exception if trying to unenroll from paid course
     */
    void unenroll(UUID userId, UUID courseId) throws IllegalAccessException;

    /**
     * Get reason why user cannot unenroll
     */
    String getUnenrollBlockReason(UUID userId, UUID courseId);

    // ============================================
    // PAID COURSE SPECIFIC LOGIC
    // ============================================

    /**
     * Check if course is paid
     */
    boolean isCoursePaid(UUID courseId);

    /**
     * Get user's enrollment count for paid courses
     */
    long countPaidEnrollments(UUID userId);

    /**
     * Get total amount spent on courses
     */
    long getTotalSpentCents(UUID userId);

    /**
     * Verify user has lifetime access (can_unenroll = false)
     */
    boolean hasLifetimeAccess(UUID userId, UUID courseId);
}
