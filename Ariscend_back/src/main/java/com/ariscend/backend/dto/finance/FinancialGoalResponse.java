package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.FinancialGoal;
import com.ariscend.backend.entity.GoalStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialGoalResponse {
    private Long id; private Long userId; private String name; private String description;
    private BigDecimal targetAmount; private BigDecimal currentAmount; private BigDecimal remainingAmount;
    private BigDecimal progressPercentage; private String currency; private LocalDate targetDate;
    private GoalStatus status; private LocalDateTime createdAt;

    public static FinancialGoalResponse from(FinancialGoal goal, BigDecimal currentAmount) {
        FinancialGoalResponse response = new FinancialGoalResponse();
        response.id = goal.getId(); response.userId = goal.getUser().getId(); response.name = goal.getName(); response.description = goal.getDescription();
        response.targetAmount = goal.getTargetAmount(); response.currentAmount = currentAmount;
        response.remainingAmount = goal.getTargetAmount().subtract(currentAmount).max(BigDecimal.ZERO);
        response.progressPercentage = currentAmount.multiply(BigDecimal.valueOf(100)).divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        response.currency = goal.getCurrency(); response.targetDate = goal.getTargetDate(); response.status = goal.getStatus(); response.createdAt = goal.getCreatedAt();
        return response;
    }
    public Long getId() { return id; } public Long getUserId() { return userId; } public String getName() { return name; }
    public String getDescription() { return description; } public BigDecimal getTargetAmount() { return targetAmount; }
    public BigDecimal getCurrentAmount() { return currentAmount; } public BigDecimal getRemainingAmount() { return remainingAmount; }
    public BigDecimal getProgressPercentage() { return progressPercentage; } public String getCurrency() { return currency; }
    public LocalDate getTargetDate() { return targetDate; } public GoalStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
