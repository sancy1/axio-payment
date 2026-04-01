// ============================================
// File: EnrollmentController.java
// Path: src/main/java/com/axioquan/payment_service/modules/enrollments/EnrollmentController.java
// ============================================

package com.axioquan.payment_service.modules.enrollments;

import com.axioquan.payment_service.utils.ApiResponse;
import com.axioquan.payment_service.modules.enrollments.dto.EnrollmentResponse;
import com.axioquan.payment_service.modules.enrollments.dto.UpdateProgressRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollments", description = "Enrollment management endpoints")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/user/{userId}/course/{courseId}")
    @Operation(
            summary = "Get enrollment",
            description = "Get enrollment details for user and course"
    )
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollment(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Get enrollment -> userId: {}, courseId: {}", userId, courseId);

        return enrollmentService.getEnrollment(userId, courseId)
                .map(enrollment -> ResponseEntity.ok(
                        ApiResponse.success("Enrollment retrieved", enrollment)
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/all")
    @Operation(
            summary = "Get user enrollments",
            description = "Get all enrollments for a user"
    )
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getUserEnrollments(
            @PathVariable UUID userId) {

        log.info("Get user enrollments -> userId: {}", userId);

        List<EnrollmentResponse> enrollments = enrollmentService.getUserEnrollments(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User enrollments retrieved", enrollments)
        );
    }

    @GetMapping("/course/{courseId}/all")
    @Operation(
            summary = "Get course enrollments",
            description = "Get all enrollments for a course"
    )
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getCourseEnrollments(
            @PathVariable UUID courseId) {

        log.info("Get course enrollments -> courseId: {}", courseId);

        List<EnrollmentResponse> enrollments = enrollmentService.getCourseEnrollments(courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Course enrollments retrieved", enrollments)
        );
    }

    @GetMapping("/payment/{paymentId}")
    @Operation(
            summary = "Get enrollment by payment",
            description = "Get enrollment created from a payment"
    )
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentByPayment(
            @PathVariable UUID paymentId) {

        log.info("Get enrollment by payment -> paymentId: {}", paymentId);

        return enrollmentService.getEnrollmentByPayment(paymentId)
                .map(enrollment -> ResponseEntity.ok(
                        ApiResponse.success("Enrollment retrieved", enrollment)
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/check-access/{userId}/{courseId}")
    @Operation(
            summary = "Check course access",
            description = "Check if user has access to course"
    )
    public ResponseEntity<ApiResponse<Boolean>> checkAccess(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Check course access -> userId: {}, courseId: {}", userId, courseId);

        boolean hasAccess = enrollmentService.hasAccess(userId, courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Access check completed", hasAccess)
        );
    }

    @GetMapping("/check-paid-access/{userId}/{courseId}")
    @Operation(
            summary = "Check paid access",
            description = "Check if user has paid for course access"
    )
    public ResponseEntity<ApiResponse<Boolean>> checkPaidAccess(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Check paid access -> userId: {}, courseId: {}", userId, courseId);

        boolean hasPaidAccess = enrollmentService.hasPaidAccess(userId, courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Paid access check completed", hasPaidAccess)
        );
    }

    // ============================================
    // Progress Tracking Endpoints
    // ============================================

    @PutMapping("/{enrollmentId}/progress")
    @Operation(
            summary = "Update enrollment progress",
            description = "Update progress for an enrollment (lessons, quizzes, assignments)"
    )
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @PathVariable UUID enrollmentId,
            @RequestBody UpdateProgressRequest request) {

        log.info("Update progress -> enrollmentId: {}", enrollmentId);

        enrollmentService.updateProgress(enrollmentId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Progress updated successfully", null)
        );
    }

    @GetMapping("/{enrollmentId}/progress")
    @Operation(
            summary = "Get enrollment progress",
            description = "Get progress percentage for an enrollment"
    )
    public ResponseEntity<ApiResponse<BigDecimal>> getProgress(
            @PathVariable UUID enrollmentId) {

        log.info("Get progress -> enrollmentId: {}", enrollmentId);

        BigDecimal progress = enrollmentService.getProgress(enrollmentId);

        return ResponseEntity.ok(
                ApiResponse.success("Progress retrieved", progress)
        );
    }

    @PostMapping("/{enrollmentId}/mark-completed")
    @Operation(
            summary = "Mark enrollment as completed",
            description = "Mark an enrollment as completed"
    )
    public ResponseEntity<ApiResponse<Void>> markAsCompleted(
            @PathVariable UUID enrollmentId) {

        log.info("Mark as completed -> enrollmentId: {}", enrollmentId);

        enrollmentService.markAsCompleted(enrollmentId);

        return ResponseEntity.ok(
                ApiResponse.success("Enrollment marked as completed", null)
        );
    }

    @GetMapping("/{enrollmentId}/is-completed")
    @Operation(
            summary = "Check if enrollment is completed",
            description = "Check if an enrollment is marked as completed"
    )
    public ResponseEntity<ApiResponse<Boolean>> isCompleted(
            @PathVariable UUID enrollmentId) {

        log.info("Is completed -> enrollmentId: {}", enrollmentId);

        boolean completed = enrollmentService.isCompleted(enrollmentId);

        return ResponseEntity.ok(
                ApiResponse.success("Completion status retrieved", completed)
        );
    }

    @GetMapping("/user/{userId}/with-progress")
    @Operation(
            summary = "Get user enrollments with progress",
            description = "Get all user enrollments with their progress information"
    )
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getUserEnrollmentsWithProgress(
            @PathVariable UUID userId) {

        log.info("Get user enrollments with progress -> userId: {}", userId);

        List<EnrollmentResponse> enrollments = enrollmentService.getUserEnrollmentsWithProgress(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User enrollments with progress retrieved", enrollments)
        );
    }

    // ============================================
    // Unenrollment Endpoints (Paid Course Control)
    // ============================================

    @GetMapping("/{userId}/{courseId}/can-unenroll")
    @Operation(
            summary = "Check if can unenroll",
            description = "Check if user can unenroll from a course (returns false for paid courses)"
    )
    public ResponseEntity<ApiResponse<Boolean>> canUnenroll(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Can unenroll -> userId: {}, courseId: {}", userId, courseId);

        boolean canUnenroll = enrollmentService.canUnenroll(userId, courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Unenrollment eligibility checked", canUnenroll)
        );
    }

    @PostMapping("/{userId}/{courseId}/unenroll")
    @Operation(
            summary = "Unenroll from course",
            description = "Remove enrollment from a course (only works for free courses)"
    )
    public ResponseEntity<ApiResponse<Void>> unenroll(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Unenroll -> userId: {}, courseId: {}", userId, courseId);

        try {
            enrollmentService.unenroll(userId, courseId);
            return ResponseEntity.ok(
                    ApiResponse.success("Unenrolled from course successfully", null)
            );
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error("Cannot unenroll: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{userId}/{courseId}/unenroll-block-reason")
    @Operation(
            summary = "Get unenrollment block reason",
            description = "Get reason why user cannot unenroll from course"
    )
    public ResponseEntity<ApiResponse<String>> getUnenrollBlockReason(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Get unenroll block reason -> userId: {}, courseId: {}", userId, courseId);

        String reason = enrollmentService.getUnenrollBlockReason(userId, courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Block reason retrieved", reason)
        );
    }

    // ============================================
    // Course Separation Endpoints (Paid vs Free)
    // ============================================

    @GetMapping("/user/{userId}/paid-courses")
    @Operation(
            summary = "Get user's paid courses",
            description = "Get all courses user has paid for (lifetime access)"
    )
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getUserPaidCourses(
            @PathVariable UUID userId) {

        log.info("Get user paid courses -> userId: {}", userId);

        List<EnrollmentResponse> paidCourses = enrollmentService.getUserPaidCourses(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Paid courses retrieved", paidCourses)
        );
    }

    @GetMapping("/user/{userId}/free-courses")
    @Operation(
            summary = "Get user's free courses",
            description = "Get all courses user is enrolled in for free"
    )
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getUserFreeCourses(
            @PathVariable UUID userId) {

        log.info("Get user free courses -> userId: {}", userId);

        List<EnrollmentResponse> freeCourses = enrollmentService.getUserFreeCourses(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Free courses retrieved", freeCourses)
        );
    }

    @GetMapping("/{userId}/{courseId}/is-paid")
    @Operation(
            summary = "Check if enrollment is paid",
            description = "Check if user has paid for this course enrollment"
    )
    public ResponseEntity<ApiResponse<Boolean>> isEnrollmentPaid(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Is enrollment paid -> userId: {}, courseId: {}", userId, courseId);

        boolean isPaid = enrollmentService.hasPaidAccess(userId, courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Enrollment payment status retrieved", isPaid)
        );
    }

    @GetMapping("/{userId}/{courseId}/has-lifetime-access")
    @Operation(
            summary = "Check lifetime access",
            description = "Check if user has lifetime access (paid course with can_unenroll=false)"
    )
    public ResponseEntity<ApiResponse<Boolean>> hasLifetimeAccess(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Has lifetime access -> userId: {}, courseId: {}", userId, courseId);

        boolean hasAccess = enrollmentService.hasLifetimeAccess(userId, courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Lifetime access status retrieved", hasAccess)
        );
    }

    // ============================================
    // Analytics Endpoints
    // ============================================

    @GetMapping("/user/{userId}/paid-courses-count")
    @Operation(
            summary = "Count paid enrollments",
            description = "Count how many paid courses user is enrolled in"
    )
    public ResponseEntity<ApiResponse<Long>> countPaidEnrollments(
            @PathVariable UUID userId) {

        log.info("Count paid enrollments -> userId: {}", userId);

        long count = enrollmentService.countPaidEnrollments(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Paid enrollment count retrieved", count)
        );
    }

    @GetMapping("/user/{userId}/total-spent")
    @Operation(
            summary = "Get total spent on courses",
            description = "Get total amount (in cents) user has spent on paid courses"
    )
    public ResponseEntity<ApiResponse<Long>> getTotalSpentCents(
            @PathVariable UUID userId) {

        log.info("Get total spent -> userId: {}", userId);

        long totalSpent = enrollmentService.getTotalSpentCents(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Total spent retrieved", totalSpent)
        );
    }
}
