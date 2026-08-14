package com.ariscend.backend.repository;

import com.ariscend.backend.entity.FinancialStatus;
import com.ariscend.backend.entity.FinancialTransaction;
import com.ariscend.backend.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    Optional<FinancialTransaction> findByIdAndUserId(Long transactionId, Long userId);
    boolean existsByUserId(Long userId);
    boolean existsByCardIdOrPaidCreditCardId(Long cardId, Long paidCreditCardId);
    boolean existsByCategoryId(Long categoryId);

    @Query("""
            SELECT movement FROM FinancialTransaction movement
            WHERE movement.user.id = :userId
              AND (:type IS NULL OR movement.type = :type)
              AND (:categoryId IS NULL OR movement.category.id = :categoryId)
              AND (:cardId IS NULL OR movement.card.id = :cardId OR movement.paidCreditCard.id = :cardId)
              AND (:currency IS NULL OR movement.currency = :currency)
              AND (:dateFrom IS NULL OR movement.transactionDate >= :dateFrom)
              AND (:dateTo IS NULL OR movement.transactionDate <= :dateTo)
              AND (:status IS NULL OR movement.status = :status)
            """)
    Page<FinancialTransaction> findByFilters(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("cardId") Long cardId,
            @Param("currency") String currency,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("status") FinancialStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(SUM(movement.amount), 0) FROM FinancialTransaction movement
            WHERE movement.status = com.ariscend.backend.entity.FinancialStatus.ACTIVE
              AND movement.type = :type
              AND movement.card.id = :cardId
            """)
    BigDecimal sumActiveByCardAndType(@Param("cardId") Long cardId, @Param("type") TransactionType type);

    @Query("""
            SELECT COALESCE(SUM(movement.amount), 0) FROM FinancialTransaction movement
            WHERE movement.status = com.ariscend.backend.entity.FinancialStatus.ACTIVE
              AND movement.type = com.ariscend.backend.entity.TransactionType.CREDIT_CARD_PAYMENT
              AND movement.paidCreditCard.id = :cardId
            """)
    BigDecimal sumActivePaymentsToCard(@Param("cardId") Long cardId);

    @Query("""
            SELECT movement.type, COALESCE(SUM(movement.baseAmount), 0)
            FROM FinancialTransaction movement
            WHERE movement.user.id = :userId
              AND movement.status = com.ariscend.backend.entity.FinancialStatus.ACTIVE
              AND movement.type IN (com.ariscend.backend.entity.TransactionType.INCOME, com.ariscend.backend.entity.TransactionType.EXPENSE)
              AND movement.transactionDate >= :dateFrom
              AND movement.transactionDate <= :dateTo
            GROUP BY movement.type
            """)
    List<Object[]> summarizeByType(@Param("userId") Long userId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT movement.category.name, COALESCE(SUM(movement.baseAmount), 0)
            FROM FinancialTransaction movement
            WHERE movement.user.id = :userId
              AND movement.status = com.ariscend.backend.entity.FinancialStatus.ACTIVE
              AND movement.type = :type
              AND movement.transactionDate >= :dateFrom
              AND movement.transactionDate <= :dateTo
            GROUP BY movement.category.name
            """)
    List<Object[]> summarizeByCategory(@Param("userId") Long userId, @Param("type") TransactionType type,
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query("""
            SELECT movement.currency, movement.type, COALESCE(SUM(movement.amount), 0)
            FROM FinancialTransaction movement
            WHERE movement.user.id = :userId
              AND movement.status = com.ariscend.backend.entity.FinancialStatus.ACTIVE
              AND movement.type IN (com.ariscend.backend.entity.TransactionType.INCOME, com.ariscend.backend.entity.TransactionType.EXPENSE)
              AND movement.transactionDate >= :dateFrom
              AND movement.transactionDate <= :dateTo
            GROUP BY movement.currency, movement.type
            """)
    List<Object[]> summarizeOriginalByCurrency(@Param("userId") Long userId,
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
}
