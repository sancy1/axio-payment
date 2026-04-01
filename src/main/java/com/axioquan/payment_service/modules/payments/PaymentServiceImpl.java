


// ============================================
// File 27: PaymentServiceImpl.java
// Path: src/main/java/com/axioquan/payment_service/modules/payments/PaymentServiceImpl.java
// ============================================

package com.axioquan.payment_service.modules.payments;

import com.axioquan.payment_service.config.PaystackProperties;
import com.axioquan.payment_service.domain.entities.Payment;
import com.axioquan.payment_service.domain.entities.Transaction;
import com.axioquan.payment_service.domain.entities.WebhookLog;
import com.axioquan.payment_service.domain.entities.Course;
import com.axioquan.payment_service.errors.PaystackApiException;
import com.axioquan.payment_service.errors.PaymentAlreadyProcessedException;
import com.axioquan.payment_service.infrastructure.paystack.PaystackClient;
import com.axioquan.payment_service.infrastructure.paystack.PaystackWebhookVerifier;
import com.axioquan.payment_service.infrastructure.paystack.dto.PaystackInitRequest;
import com.axioquan.payment_service.infrastructure.paystack.dto.PaystackInitResponse;
import com.axioquan.payment_service.infrastructure.paystack.dto.PaystackVerifyResponse;
import com.axioquan.payment_service.modules.payments.dto.*;
import com.axioquan.payment_service.modules.transactions.TransactionService;
import com.axioquan.payment_service.modules.transactions.dto.CreateTransactionRequest;
import com.axioquan.payment_service.modules.transactions.TransactionRepository;
import com.axioquan.payment_service.modules.webhooks.WebhookLogRepository;
import com.axioquan.payment_service.modules.webhooks.dto.WebhookPayload;
import com.axioquan.payment_service.modules.courses.CourseRepository;
import com.axioquan.payment_service.modules.enrollments.EnrollmentRepository;
import com.axioquan.payment_service.modules.notifications.NotificationService;
import com.axioquan.payment_service.utils.CurrencyConverter;
import com.axioquan.payment_service.utils.UserCurrencyResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final WebhookLogRepository webhookLogRepository;
    private final PaystackClient paystackClient;
    private final PaystackProperties paystackProperties;
    private final PaystackWebhookVerifier webhookVerifier;
    private final ObjectMapper objectMapper;

    // ✅ NEW (correct)
    private final CourseRepository courseRepository;
    // ✅ NEW: Enrollment repository for enrollment verification
    private final EnrollmentRepository enrollmentRepository;

    // ✅ NEW: Notification service for in-app notification creation
    private final NotificationService notificationService;

    // ✅ Currency tools
    private final CurrencyConverter currencyConverter;
    private final UserCurrencyResolver userCurrencyResolver;

    private static final String SUCCESS = "SUCCESS";
    private static final String PENDING = "PENDING";
    private static final String FAILED = "FAILED";

    // ============================================
    // REQUIRED METHODS
    // ============================================

    @Override
    public VerifyPaymentResponse getPaymentByReference(String reference) {
        Payment payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return buildResponse(payment);
    }

    @Override
    public boolean hasUserPurchasedCourse(UUID userId, UUID courseId) {
        return paymentRepository.existsByUserIdAndCourseIdAndStatus(
                userId, courseId, SUCCESS
        );
    }

    // ============================================
    // INITIALIZE PAYMENT (FIXED VERSION)
    // ============================================

    @Override
    public InitializePaymentResponse initializePayment(InitializePaymentRequest request) {

        log.info("Initializing payment for user: {}, course: {}",
                request.getUserId(), request.getCourseId());

        if (hasUserPurchasedCourse(request.getUserId(), request.getCourseId())) {
            throw new PaymentAlreadyProcessedException("Course already purchased");
        }

        // ✅ 1. FETCH COURSE FROM DATABASE
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // ============================================
        // ✅ 2. HANDLE FREE COURSE
        // ============================================
        if (course.getPriceCents() == null || course.getPriceCents() == 0) {

            String reference = generateReference();

            Payment payment = Payment.builder()
                    .reference(reference)
                    .userId(request.getUserId())
                    .courseId(request.getCourseId())
                    .amountCents(0)
                    .currency("NGN")
                    .originalCurrency("NGN")
                    .originalAmountCents(0)
                    .status(SUCCESS)
                    .build();

            paymentRepository.save(payment);

            log.info("Free course granted. Reference: {}", reference);

            return InitializePaymentResponse.builder()
                    .reference(reference)
                    .authorizationUrl(null)
                    .accessCode(null)
                    .build();
        }

        // ============================================
        // ✅ 3. DETERMINE USER CURRENCY
        // ============================================
        String userCurrency = userCurrencyResolver.resolve(request.getEmail());

        // ============================================
        // ✅ 4. CONVERT PRICE
        // ============================================
        BigDecimal convertedAmount = currencyConverter.convert(
                BigDecimal.valueOf(course.getPriceCents()),
                "NGN",
                userCurrency
        );

        int amountInCents = convertedAmount
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        String reference = generateReference();

        // ============================================
        // ✅ 5. SAVE PAYMENT
        // ============================================
        Payment payment = Payment.builder()
                .reference(reference)
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .amountCents(amountInCents)
                .currency(userCurrency)
                .originalCurrency("NGN")
                .originalAmountCents(course.getPriceCents())
                .status(PENDING)
                .build();

        paymentRepository.save(payment);

        // ============================================
        // ✅ 6. PAYSTACK REQUEST
        // ============================================
        Map<String, Object> metadata = Map.of(
                "user_id", request.getUserId().toString(),
                "course_id", request.getCourseId().toString(),
                "payment_id", payment.getId().toString()
        );

        PaystackInitRequest initRequest = PaystackInitRequest.builder()
                .email(request.getEmail())
                .amount((long) amountInCents)
                .reference(reference)
                .currency(userCurrency)
                .callbackUrl(paystackProperties.getCallbackUrl())
                .metadata(metadata)
                .build();

        // ============================================
        // ✅ 7. CALL PAYSTACK
        // ============================================
        try {
            PaystackInitResponse response =
                    paystackClient.initializeTransaction(initRequest);

            payment.setPaystackReference(response.getData().getReference());
            paymentRepository.save(payment);

            return InitializePaymentResponse.builder()
                    .reference(reference)
                    .authorizationUrl(response.getData().getAuthorizationUrl())
                    .accessCode(response.getData().getAccessCode())
                    .build();

        } catch (Exception e) {
            payment.markAsFailed();
            paymentRepository.save(payment);

            log.error("Payment initialization failed", e);

            throw new PaystackApiException("Initialization failed", e);
        }
    }

    // ============================================
    // VERIFY PAYMENT
    // ============================================

    @Override
    public VerifyPaymentResponse verifyPayment(String reference) {

        Payment payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.isSuccessful()) {
            return buildResponse(payment);
        }

        try {
            PaystackVerifyResponse response =
                    paystackClient.verifyTransaction(reference);

            if (response.isStatus()
                    && "success".equalsIgnoreCase(response.getData().getStatus())) {

                payment.markAsSuccess();
                payment.setPaymentMethod(response.getData().getChannel());
                payment.setPaystackResponse(objectMapper.writeValueAsString(response));

                paymentRepository.save(payment);

                createAuditTransaction(payment);

                // ✅ NEW: Create in-app notification for payment success
                createPaymentSuccessNotification(payment);

                return buildResponse(payment);
            }

            payment.markAsFailed();
            paymentRepository.save(payment);

            return VerifyPaymentResponse.builder()
                    .reference(reference)
                    .status(FAILED)
                    .build();

        } catch (Exception e) {
            throw new PaystackApiException("Verification failed", e);
        }
    }

    // ============================================
    // WEBHOOK
    // ============================================

    @Override
    public void processWebhook(WebhookPayload payload) {

        WebhookLog logEntry = WebhookLog.builder()
                .eventType(payload.getEvent())
                .payload(serialize(payload))
                .processed(false)
                .build();

        webhookLogRepository.save(logEntry);

        try {
            if ("charge.success".equals(payload.getEvent())) {
                handleSuccess(payload);
            } else if ("charge.failed".equals(payload.getEvent())) {
                handleFailure(payload);
            }

            logEntry.markProcessed();

        } catch (Exception e) {
            logEntry.markFailed(e.getMessage());
            throw e;
        } finally {
            webhookLogRepository.save(logEntry);
        }
    }

    private void handleSuccess(WebhookPayload payload) {
        if (payload.getData() == null) return;

        String ref = payload.getData().getReference();

        paymentRepository.findByReference(ref).ifPresent(payment -> {
            if (!payment.isSuccessful()) {
                payment.markAsSuccess();
                paymentRepository.save(payment);
                createAuditTransaction(payment);

                // ✅ NEW: Create in-app notification for payment success
                createPaymentSuccessNotification(payment);
            }
        });
    }

    private void handleFailure(WebhookPayload payload) {
        if (payload.getData() == null) return;

        String ref = payload.getData().getReference();

        paymentRepository.findByReference(ref).ifPresent(payment -> {
            if (payment.isPending()) {
                payment.markAsFailed();
                paymentRepository.save(payment);
            }
        });
    }

    private void createAuditTransaction(Payment payment) {
        log.info("Creating audit transaction for payment: {}", payment.getReference());
        
        try {
            String transactionReference = "TXN_" + payment.getReference();
            
            // Use TransactionService to create the transaction
            CreateTransactionRequest request = new CreateTransactionRequest(
                    payment.getUserId(),
                    payment.getId(),
                    "PAYMENT",
                    payment.getAmountCents(),
                    payment.getCurrency(),
                    payment.getOriginalCurrency(),
                    payment.getOriginalAmountCents(),
                    payment.getExchangeRate(),
                    "COMPLETED",
                    transactionReference,
                    null
            );
            
            transactionService.createTransaction(request);
            log.info("Audit transaction created successfully with reference: {}", transactionReference);
            
        } catch (Exception e) {
            // Log error but don't fail the payment process if transaction creation fails
            log.error("Failed to create audit transaction for payment: {}", payment.getReference(), e);
        }
    }

    /**
     * Create an in-app notification for successful payment.
     */
    private void createPaymentSuccessNotification(Payment payment) {
        log.info("Creating payment success notification for user: {}", payment.getUserId());

        try {
            // Fetch course details for the notification
            String courseName = courseRepository.findById(payment.getCourseId())
                    .map(Course::getTitle)
                    .orElse("Your course");

            // Create the in-app notification
            String message = String.format("✅ Payment for %s received!", courseName);
            String data = String.format("{\"%s\": \"%s\", \"%s\": \"%s\"}", 
                    "courseId", payment.getCourseId().toString(),
                    "paymentReference", payment.getReference());

            notificationService.createNotification(
                    payment.getUserId(),
                    "PAYMENT_SUCCESS",
                    message,
                    data
            );

            log.info("Payment success notification created for user: {}", payment.getUserId());

        } catch (Exception e) {
            // Log error but don't fail the payment process if notification creation fails
            log.error("Failed to create payment success notification for user: {}", payment.getUserId(), e);
        }
    }

    private VerifyPaymentResponse buildResponse(Payment payment) {
        // ✅ Build base response
        VerifyPaymentResponse.VerifyPaymentResponseBuilder builder = VerifyPaymentResponse.builder()
                .reference(payment.getReference())
                .status(payment.getStatus())
                .amountPaid(payment.getAmountNgn())
                .currency(payment.getCurrency())
                .paidAt(payment.getPaidAt())
                .userId(payment.getUserId())
                .courseId(payment.getCourseId())
                .isPaid(payment.isSuccessful());

        // ✅ Add enrollment details if available
        enrollmentRepository.findByPaymentId(payment.getId())
                .ifPresent(enrollment -> {
                    builder.enrollmentId(enrollment.getId());
                    builder.enrollmentStatus(enrollment.getStatus());
                    builder.hasEnrollment(true);
                });

        return builder.build();
    }

    private String generateReference() {
        return "PAY_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}