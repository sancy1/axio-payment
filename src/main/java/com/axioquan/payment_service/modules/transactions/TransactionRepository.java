// ============================================
// File 23: TransactionRepository.java
// Path: src/main/java/com/axioquan/payment_service/modules/transactions/TransactionRepository.java
// ============================================

package com.axioquan.payment_service.modules.transactions;

import com.axioquan.payment_service.domain.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReference(String reference);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Transaction> findByPaymentId(UUID paymentId);

    List<Transaction> findByTransactionTypeAndStatus(String transactionType, String status);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionType = 'PAYMENT' AND t.status = 'COMPLETED'")
    long countSuccessfulPayments();
}