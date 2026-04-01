package com.axioquan.payment_service.modules.transactions;

import com.axioquan.payment_service.modules.transactions.dto.TransactionDetailResponse;
import com.axioquan.payment_service.modules.transactions.dto.TransactionListResponse;
import com.axioquan.payment_service.modules.transactions.dto.CreateTransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for transaction management endpoints.
 * Provides APIs for transaction retrieval and analysis.
 */
@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    // ========== CREATE ENDPOINT ==========

    /**
     * Create a new transaction record.
     * 
     * POST /api/v1/transactions
     * 
     * @param request The transaction creation request
     * @return Created transaction details
     */
    @PostMapping
    public ResponseEntity<TransactionDetailResponse> createTransaction(
            @RequestBody CreateTransactionRequest request) {
        log.info("Creating transaction with reference: {}", request.reference());
        TransactionDetailResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== GET BY ID ENDPOINTS ==========

    /**
     * Get a transaction by its ID.
     * 
     * GET /api/v1/transactions/{id}
     * 
     * @param id The transaction ID
     * @return Transaction details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailResponse> getTransactionById(@PathVariable UUID id) {
        log.debug("Fetching transaction by ID: {}", id);
        TransactionDetailResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a transaction by its reference.
     * 
     * GET /api/v1/transactions/reference/{reference}
     * 
     * @param reference The transaction reference
     * @return Transaction details
     */
    @GetMapping("/reference/{reference}")
    public ResponseEntity<TransactionDetailResponse> getTransactionByReference(
            @PathVariable String reference) {
        log.debug("Fetching transaction by reference: {}", reference);
        TransactionDetailResponse response = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(response);
    }

    // ========== GET USER TRANSACTIONS ==========

    /**
     * Get all transactions for a specific user.
     * Results are ordered by creation date (newest first).
     * 
     * GET /api/v1/transactions/user/{userId}
     * 
     * @param userId The user ID
     * @return List of user transactions
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionListResponse>> getUserTransactions(
            @PathVariable UUID userId) {
        log.debug("Fetching transactions for user: {}", userId);
        List<TransactionListResponse> response = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction summary for a user.
     * Includes counts of different transaction statuses and total amounts.
     * 
     * GET /api/v1/transactions/user/{userId}/summary
     * 
     * @param userId The user ID
     * @return User transaction summary
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<TransactionService.UserTransactionSummary> getUserTransactionSummary(
            @PathVariable UUID userId) {
        log.debug("Fetching transaction summary for user: {}", userId);
        TransactionService.UserTransactionSummary response = 
                transactionService.getUserTransactionSummary(userId);
        return ResponseEntity.ok(response);
    }

    // ========== GET PAYMENT TRANSACTIONS ==========

    /**
     * Get all transactions for a specific payment.
     * 
     * GET /api/v1/transactions/payment/{paymentId}
     * 
     * @param paymentId The payment ID
     * @return List of payment transactions
     */
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<TransactionListResponse>> getPaymentTransactions(
            @PathVariable UUID paymentId) {
        log.debug("Fetching transactions for payment: {}", paymentId);
        List<TransactionListResponse> response = transactionService.getPaymentTransactions(paymentId);
        return ResponseEntity.ok(response);
    }

    // ========== GET BY TYPE AND STATUS ==========

    /**
     * Get transactions filtered by type and status.
     * 
     * GET /api/v1/transactions/filter?type={type}&status={status}
     * 
     * @param type The transaction type (e.g., PAYMENT, REFUND)
     * @param status The transaction status (e.g., COMPLETED, PENDING, FAILED)
     * @return List of filtered transactions
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TransactionListResponse>> getTransactionsByTypeAndStatus(
            @RequestParam String type,
            @RequestParam String status) {
        log.debug("Fetching transactions - Type: {}, Status: {}", type, status);
        List<TransactionListResponse> response = 
                transactionService.getTransactionsByTypeAndStatus(type, status);
        return ResponseEntity.ok(response);
    }

    // ========== GET BY DATE RANGE ==========

    /**
     * Get transactions within a date range.
     * 
     * GET /api/v1/transactions/date-range?startDate={startDate}&endDate={endDate}
     * Date format: yyyy-MM-dd'T'HH:mm:ss (e.g., 2024-01-15T08:00:00)
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of transactions within the date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionListResponse>> getTransactionsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Fetching transactions between {} and {}", startDate, endDate);
        List<TransactionListResponse> response = 
                transactionService.getTransactionsBetween(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ========== ANALYTICS ENDPOINTS ==========

    /**
     * Get count of successful payment transactions.
     * 
     * GET /api/v1/transactions/analytics/successful-count
     * 
     * @return Count of successful transactions
     */
    @GetMapping("/analytics/successful-count")
    public ResponseEntity<AnalyticsResponse> getSuccessfulPaymentCount() {
        log.debug("Fetching successful payment count");
        long count = transactionService.countSuccessfulPayments();
        return ResponseEntity.ok(new AnalyticsResponse("successful_transactions", count));
    }

    // ========== HELPER DTOs ==========

    /**
     * DTO for analytics responses
     */
    public record AnalyticsResponse(String metric, Long value) {}
}
