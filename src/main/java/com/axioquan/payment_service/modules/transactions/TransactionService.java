package com.axioquan.payment_service.modules.transactions;

import com.axioquan.payment_service.modules.transactions.dto.TransactionDetailResponse;
import com.axioquan.payment_service.modules.transactions.dto.TransactionListResponse;
import com.axioquan.payment_service.modules.transactions.dto.CreateTransactionRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for transaction management.
 * Handles creation, retrieval, and analysis of payment transactions.
 */
public interface TransactionService {

    /**
     * Create a new transaction record.
     * @param request The transaction creation request
     * @return The created transaction response
     */
    TransactionDetailResponse createTransaction(CreateTransactionRequest request);

    /**
     * Get a transaction by its unique reference.
     * @param reference The transaction reference
     * @return The transaction details
     */
    TransactionDetailResponse getTransactionByReference(String reference);

    /**
     * Get a transaction by its ID.
     * @param transactionId The transaction ID
     * @return The transaction details
     */
    TransactionDetailResponse getTransactionById(UUID transactionId);

    /**
     * Get all transactions for a specific user, ordered by creation date (newest first).
     * @param userId The user ID
     * @return List of transactions
     */
    List<TransactionListResponse> getUserTransactions(UUID userId);

    /**
     * Get all transactions for a specific payment.
     * @param paymentId The payment ID
     * @return List of transactions
     */
    List<TransactionListResponse> getPaymentTransactions(UUID paymentId);

    /**
     * Get transactions by type and status.
     * @param transactionType The transaction type (e.g., PAYMENT, REFUND)
     * @param status The transaction status (e.g., COMPLETED, PENDING)
     * @return List of transactions
     */
    List<TransactionListResponse> getTransactionsByTypeAndStatus(String transactionType, String status);

    /**
     * Get transactions within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of transactions
     */
    List<TransactionListResponse> getTransactionsBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count successful payment transactions.
     * @return The count of successful transactions
     */
    long countSuccessfulPayments();

    /**
     * Get user transaction summary.
     * @param userId The user ID
     * @return Transaction summary info
     */
    UserTransactionSummary getUserTransactionSummary(UUID userId);

    /**
     * Check if a transaction reference already exists.
     * @param reference The transaction reference
     * @return true if exists, false otherwise
     */
    boolean transactionReferenceExists(String reference);

    /**
     * DTO for user transaction summary
     */
    record UserTransactionSummary(
        UUID userId,
        long totalTransactions,
        long completedTransactions,
        long pendingTransactions,
        long failedTransactions,
        Long totalAmountCents,
        String currency
    ) {}
}
