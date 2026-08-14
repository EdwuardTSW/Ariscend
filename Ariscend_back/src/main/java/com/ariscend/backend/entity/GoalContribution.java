package com.ariscend.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_contributions")
public class GoalContribution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private FinancialGoal goal;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;
    @Column(name = "contribution_date", nullable = false)
    private LocalDate contributionDate;
    @Column(length = 500)
    private String notes;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private FinancialTransaction linkedTransaction;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinancialStatus status = FinancialStatus.ACTIVE;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public GoalContribution() {}
    @PrePersist public void assignCreatedAt() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public FinancialGoal getGoal() { return goal; } public void setGoal(FinancialGoal goal) { this.goal = goal; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getExchangeRate() { return exchangeRate; } public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public LocalDate getContributionDate() { return contributionDate; } public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }
    public String getNotes() { return notes; } public void setNotes(String notes) { this.notes = notes; }
    public FinancialTransaction getLinkedTransaction() { return linkedTransaction; } public void setLinkedTransaction(FinancialTransaction linkedTransaction) { this.linkedTransaction = linkedTransaction; }
    public FinancialStatus getStatus() { return status; } public void setStatus(FinancialStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; } public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
}
