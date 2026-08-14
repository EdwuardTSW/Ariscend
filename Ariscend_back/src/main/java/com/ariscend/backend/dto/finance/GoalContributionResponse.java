package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.FinancialStatus;
import com.ariscend.backend.entity.GoalContribution;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalContributionResponse {
    private Long id; private Long goalId; private Long transactionId; private BigDecimal amount;
    private String currency; private BigDecimal exchangeRate; private LocalDate contributionDate;
    private String notes; private FinancialStatus status; private LocalDateTime createdAt;
    public static GoalContributionResponse from(GoalContribution contribution) {
        GoalContributionResponse response = new GoalContributionResponse();
        response.id = contribution.getId(); response.goalId = contribution.getGoal().getId();
        response.transactionId = contribution.getLinkedTransaction().getId(); response.amount = contribution.getAmount();
        response.currency = contribution.getGoal().getCurrency(); response.exchangeRate = contribution.getExchangeRate();
        response.contributionDate = contribution.getContributionDate(); response.notes = contribution.getNotes();
        response.status = contribution.getStatus(); response.createdAt = contribution.getCreatedAt(); return response;
    }
    public Long getId() { return id; } public Long getGoalId() { return goalId; } public Long getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; } public String getCurrency() { return currency; } public BigDecimal getExchangeRate() { return exchangeRate; }
    public LocalDate getContributionDate() { return contributionDate; } public String getNotes() { return notes; }
    public FinancialStatus getStatus() { return status; } public LocalDateTime getCreatedAt() { return createdAt; }
}
