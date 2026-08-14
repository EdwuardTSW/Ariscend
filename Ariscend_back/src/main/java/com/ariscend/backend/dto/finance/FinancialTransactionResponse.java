package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.FinancialStatus;
import com.ariscend.backend.entity.FinancialTransaction;
import com.ariscend.backend.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialTransactionResponse {
    private Long id; private Long userId; private TransactionType type;
    private Long categoryId; private String categoryName; private Long cardId; private Long paidCreditCardId;
    private BigDecimal amount; private String currency; private BigDecimal exchangeRate; private BigDecimal baseAmount;
    private String description; private LocalDate transactionDate; private FinancialStatus status;
    private boolean goalGenerated; private LocalDateTime createdAt; private LocalDateTime cancelledAt;

    public static FinancialTransactionResponse from(FinancialTransaction transaction) {
        FinancialTransactionResponse response = new FinancialTransactionResponse();
        response.id = transaction.getId(); response.userId = transaction.getUser().getId(); response.type = transaction.getType();
        if (transaction.getCategory() != null) { response.categoryId = transaction.getCategory().getId(); response.categoryName = transaction.getCategory().getName(); }
        if (transaction.getCard() != null) response.cardId = transaction.getCard().getId();
        if (transaction.getPaidCreditCard() != null) response.paidCreditCardId = transaction.getPaidCreditCard().getId();
        response.amount = transaction.getAmount(); response.currency = transaction.getCurrency(); response.exchangeRate = transaction.getExchangeRate();
        response.baseAmount = transaction.getBaseAmount(); response.description = transaction.getDescription(); response.transactionDate = transaction.getTransactionDate();
        response.status = transaction.getStatus(); response.goalGenerated = transaction.isGoalGenerated(); response.createdAt = transaction.getCreatedAt(); response.cancelledAt = transaction.getCancelledAt();
        return response;
    }
    public Long getId() { return id; } public Long getUserId() { return userId; } public TransactionType getType() { return type; }
    public Long getCategoryId() { return categoryId; } public String getCategoryName() { return categoryName; }
    public Long getCardId() { return cardId; } public Long getPaidCreditCardId() { return paidCreditCardId; }
    public BigDecimal getAmount() { return amount; } public String getCurrency() { return currency; }
    public BigDecimal getExchangeRate() { return exchangeRate; } public BigDecimal getBaseAmount() { return baseAmount; }
    public String getDescription() { return description; } public LocalDate getTransactionDate() { return transactionDate; }
    public FinancialStatus getStatus() { return status; } public boolean isGoalGenerated() { return goalGenerated; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getCancelledAt() { return cancelledAt; }
}
