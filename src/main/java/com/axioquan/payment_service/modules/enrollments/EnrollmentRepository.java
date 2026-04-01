// ============================================
// File: EnrollmentRepository.java
// Path: src/main/java/com/axioquan/payment_service/modules/enrollments/EnrollmentRepository.java
// ============================================

package com.axioquan.payment_service.modules.enrollments;

import com.axioquan.payment_service.domain.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    /**
     * Find enrollment by user and course
     */
    @Query(value = "SELECT * FROM enrollments WHERE user_id = CAST(:userId AS UUID) AND course_id = CAST(:courseId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, UUID courseId);

    /**
     * Check if user has active enrollment in course
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.userId = :userId AND e.courseId = :courseId AND e.status = 'active'")
    boolean existsActiveEnrollment(UUID userId, UUID courseId);

    /**
     * Check if user is enrolled (regardless of status)
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.userId = :userId AND e.courseId = :courseId")
    boolean existsEnrollment(UUID userId, UUID courseId);

    /**
     * Find all enrollments for user
     */
    @Query(value = "SELECT * FROM enrollments WHERE user_id = CAST(:userId AS UUID) ORDER BY enrolled_at DESC", nativeQuery = true)
    List<Enrollment> findByUserId(UUID userId);

    /**
     * Find all enrollments for course
     */
    @Query(value = "SELECT * FROM enrollments WHERE course_id = CAST(:courseId AS UUID) ORDER BY enrolled_at DESC", nativeQuery = true)
    List<Enrollment> findByCourseId(UUID courseId);

    /**
     * Find enrollment by payment ID
     */
    @Query(value = "SELECT * FROM enrollments WHERE payment_id = CAST(:paymentId AS UUID) LIMIT 1", nativeQuery = true)
    Optional<Enrollment> findByPaymentId(UUID paymentId);

    /**
     * Check if user has paid enrollment
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.userId = :userId AND e.courseId = :courseId AND e.enrolledPriceCents > 0")
    boolean hasPaidEnrollment(UUID userId, UUID courseId);

    /**
     * Get total paid enrollments for user
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.userId = :userId AND e.enrolledPriceCents > 0 AND e.status = 'active'")
    long countPaidEnrollments(UUID userId);

    /**
     * Find all completed enrollments for user
     */
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.completedAt IS NOT NULL ORDER BY e.completedAt DESC")
    List<Enrollment> findCompletedByUserId(UUID userId);

    /**
     * Find all in-progress enrollments for user
     */
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.status = 'active' AND e.completedAt IS NULL ORDER BY e.lastAccessedAt DESC")
    List<Enrollment> findInProgressByUserId(UUID userId);

    /**
     * Check if enrollment is completed
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.id = :enrollmentId AND e.completedAt IS NOT NULL")
    boolean isCompleted(UUID enrollmentId);

    /**
     * Get course enrollment count
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = 'active'")
    long countActiveEnrollments(UUID courseId);

    /**
     * Check if any paid enrollments exist for user (PAID COURSE CRITICAL)
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.userId = :userId AND e.enrolledPriceCents > 0 AND e.status = 'active'")
    boolean userHasPaidEnrollments(UUID userId);

    /**
     * Find all paid enrollments for user
     */
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND e.enrolledPriceCents > 0 AND e.status = 'active' ORDER BY e.enrolledAt DESC")
    List<Enrollment> findPaidEnrollmentsByUserId(UUID userId);

    /**
     * Find all free enrollments for user
     */
    @Query("SELECT e FROM Enrollment e WHERE e.userId = :userId AND (e.enrolledPriceCents = 0 OR e.enrolledPriceCents IS NULL) AND e.status = 'active' ORDER BY e.enrolledAt DESC")
    List<Enrollment> findFreeEnrollmentsByUserId(UUID userId);

    /**
     * Check if user has lifetime access to course (PAID COURSE CRITICAL)
     */
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.userId = :userId AND e.courseId = :courseId AND e.enrolledPriceCents > 0 AND e.canUnenroll = false")
    boolean hasLifetimeAccess(UUID userId, UUID courseId);

    /**
     * Get total spent by user in cents
     */
    @Query("SELECT COALESCE(SUM(e.enrolledPriceCents), 0) FROM Enrollment e WHERE e.userId = :userId AND e.enrolledPriceCents > 0")
    long getTotalSpentCents(UUID userId);
}
