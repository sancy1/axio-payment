package com.axioquan.payment_service.modules.transactions;

import com.axioquan.payment_service.domain.entities.Transaction;
import com.axioquan.payment_service.modules.transactions.dto.TransactionDetailResponse;
import com.axioquan.payment_service.modules.transactions.dto.TransactionListResponse;
import com.axioquan.payment_service.modules.transactions.dto.CreateTransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of TransactionService.
 * Provides business logic for transaction management.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public TransactionDetailResponse createTransaction(CreateTransactionRequest request) {
        log.info("Creating new transaction with reference: {}", request.reference());

        // Check if reference already exists
        if (transactionReferenceExists(request.reference())) {
            log.warn("Transaction with reference {} already exists", request.reference());
            throw new IllegalStateException("Transaction reference already exists: " + request.reference());
        }

        // Build and save the transaction
        Transaction transaction = Transaction.builder()
                .userId(request.userId())
                .paymentId(request.paymentId())
                .transactionType(request.transactionType())
                .amountCents(request.amountCents())
                .currency(request.currency())
                .originalCurrency(request.originalCurrency())
                .originalAmountCents(request.originalAmountCents())
                .exchangeRate(request.exchangeRate())
                .status(request.status())
                .reference(request.reference())
                .paystackData(request.paystackData())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());

        return mapToDetailResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionByReference(String reference) {
        log.debug("Fetching transaction by reference: {}", reference);

        Transaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Transaction not found with reference: " + reference));

        return mapToDetailResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionById(UUID transactionId) {
        log.debug("Fetching transaction by ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        return mapToDetailResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionListResponse> getUserTransactions(UUID userId) {
        log.debug("Fetching transactions for user: {}", userId);

        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionListResponse> getPaymentTransactions(UUID paymentId) {
        log.debug("Fetching transactions for payment: {}", paymentId);

        return transactionRepository.findByPaymentId(paymentId)
                .stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionListResponse> getTransactionsByTypeAndStatus(String transactionType, String status) {
        log.debug("Fetching transactions - Type: {}, Status: {}", transactionType, status);

        return transactionRepository.findByTransactionTypeAndStatus(transactionType, status)
                .stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionListResponse> getTransactionsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching transactions between {} and {}", startDate, endDate);

        return transactionRepository.findTransactionsBetween(startDate, endDate)
                .stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countSuccessfulPayments() {
        log.debug("Counting successful payments");
        return transactionRepository.countSuccessfulPayments();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionService.UserTransactionSummary getUserTransactionSummary(UUID userId) {
        log.debug("Getting transaction summary for user: {}", userId);

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        long totalTransactions = transactions.size();
        long completedTransactions = transactions.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();
        long pendingTransactions = transactions.stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .count();
        long failedTransactions = transactions.stream()
                .filter(t -> "FAILED".equals(t.getStatus()))
                .count();

        Long totalAmountCents = transactions.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .mapToLong(Transaction::getAmountCents)
                .boxed()
                .map(Long::valueOf)
                .reduce(0L, Long::sum);

        String currency = transactions.isEmpty() ? "NGN" : transactions.get(0).getCurrency();

        return new TransactionService.UserTransactionSummary(
                userId,
                totalTransactions,
                completedTransactions,
                pendingTransactions,
                failedTransactions,
                totalAmountCents,
                currency
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean transactionReferenceExists(String reference) {
        return transactionRepository.findByReference(reference).isPresent();
    }

    // ========== HELPER METHODS ==========

    /**
     * Map Transaction entity to TransactionDetailResponse DTO
     */
    private TransactionDetailResponse mapToDetailResponse(Transaction transaction) {
        return new TransactionDetailResponse(
                transaction.getId(),
                transaction.getPaymentId(),
                transaction.getUserId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getAmountCents(),
                transaction.getCurrency(),
                transaction.getOriginalCurrency(),
                transaction.getOriginalAmountCents(),
                transaction.getExchangeRate(),
                transaction.getStatus(),
                transaction.getReference(),
                transaction.getPaystackData(),
                transaction.getCreatedAt()
        );
    }

    /**
     * Map Transaction entity to TransactionListResponse DTO
     */
    private TransactionListResponse mapToListResponse(Transaction transaction) {
        return new TransactionListResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getReference(),
                transaction.getCreatedAt()
        );
    }
}
