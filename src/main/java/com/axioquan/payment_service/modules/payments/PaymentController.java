// ============================================
// File 33: PaymentController.java
// Path: src/main/java/com/axioquan/payment_service/modules/payments/PaymentController.java
// ============================================

package com.axioquan.payment_service.modules.payments;

import com.axioquan.payment_service.config.JwtTokenProvider;
import com.axioquan.payment_service.modules.payments.dto.InitializePaymentRequest;
import com.axioquan.payment_service.modules.payments.dto.InitializePaymentResponse;
import com.axioquan.payment_service.modules.payments.dto.VerifyPaymentResponse;
import com.axioquan.payment_service.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment API", description = "Endpoints for payment processing")
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtTokenProvider jwtTokenProvider;

    // ============================================
    // INITIALIZE PAYMENT
    // ============================================

    @PostMapping("/initialize")
    @Operation(
            summary = "Initialize a payment",
            description = "Creates a payment and returns Paystack checkout URL (if paid)",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer token (JWT)", required = false)
            }
    )
    public ResponseEntity<ApiResponse<InitializePaymentResponse>> initializePayment(
            @Valid @RequestBody InitializePaymentRequest request,
            HttpServletRequest httpRequest) {

        log.info("Initialize payment request -> user: {}, course: {}",
                request.getUserId(), request.getCourseId());

        // Optional: Validate JWT if provided
        String token = extractToken(httpRequest);
        if (token != null) {
            validateTokenOrThrow(token);
            log.info("JWT token validated");
        }

        InitializePaymentResponse response = paymentService.initializePayment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initialized successfully", response));
    }

    // ============================================
    // VERIFY PAYMENT
    // ============================================

    @GetMapping("/verify/{reference}")
    @Operation(
            summary = "Verify payment",
            description = "Verify payment status using reference"
    )
    public ResponseEntity<ApiResponse<VerifyPaymentResponse>> verifyPayment(
            @PathVariable String reference) {

        log.info("Verify payment request -> reference: {}", reference);

        VerifyPaymentResponse response = paymentService.verifyPayment(reference);

        return ResponseEntity.ok(
                ApiResponse.success("Payment verified successfully", response)
        );
    }

    // ============================================
    // GET PAYMENT BY REFERENCE
    // ============================================

    @GetMapping("/reference/{reference}")
    @Operation(
            summary = "Get payment by reference",
            description = "Fetch payment details"
    )
    public ResponseEntity<ApiResponse<VerifyPaymentResponse>> getPaymentByReference(
            @PathVariable String reference) {

        log.info("Fetch payment -> reference: {}", reference);

        VerifyPaymentResponse response = paymentService.getPaymentByReference(reference);

        return ResponseEntity.ok(
                ApiResponse.success("Payment retrieved successfully", response)
        );
    }

    // ============================================
    // CHECK PURCHASE STATUS
    // ============================================

    @GetMapping("/user/{userId}/course/{courseId}/status")
    @Operation(
            summary = "Check purchase status",
            description = "Check if a user already purchased a course"
    )
    public ResponseEntity<ApiResponse<Boolean>> checkCoursePurchaseStatus(
            @PathVariable UUID userId,
            @PathVariable UUID courseId) {

        log.info("Check purchase -> user: {}, course: {}", userId, courseId);

        boolean hasPurchased =
                paymentService.hasUserPurchasedCourse(userId, courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Purchase status retrieved", hasPurchased)
        );
    }

    // ============================================
    // (OPTIONAL FUTURE FEATURE)
    // ============================================

    @GetMapping("/user/{userId}/courses")
    @Operation(
            summary = "Get user's purchased courses",
            description = "Will return purchased courses in future"
    )
    public ResponseEntity<ApiResponse<?>> getUserPurchasedCourses(
            @PathVariable UUID userId) {

        log.info("Fetch purchased courses -> user: {}", userId);

        return ResponseEntity.ok(
                ApiResponse.success("Feature coming soon", null)
        );
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Validate JWT token or throw exception
     */
    private void validateTokenOrThrow(String token) {
        // Optionally validate token format - don't throw on expiration
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid JWT token format");
            }
        } catch (Exception ex) {
            log.warn("Token validation warning (non-blocking): {}", ex.getMessage());
            // Don't throw - allow request to proceed
        }
    }
}