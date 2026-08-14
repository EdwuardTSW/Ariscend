package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.CardType;
import com.ariscend.backend.entity.PaymentAlertStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardSummaryResponse {
    private Long cardId;
    private CardType type;
    private String currency;
    private BigDecimal currentBalance;
    private BigDecimal currentDebt;
    private BigDecimal availableCredit;
    private LocalDate nextClosingDate;
    private LocalDate nextPaymentDate;
    private Long daysUntilPayment;
    private PaymentAlertStatus paymentAlertStatus;

    public Long getCardId() { return cardId; } public void setCardId(Long value) { cardId = value; }
    public CardType getType() { return type; } public void setType(CardType value) { type = value; }
    public String getCurrency() { return currency; } public void setCurrency(String value) { currency = value; }
    public BigDecimal getCurrentBalance() { return currentBalance; } public void setCurrentBalance(BigDecimal value) { currentBalance = value; }
    public BigDecimal getCurrentDebt() { return currentDebt; } public void setCurrentDebt(BigDecimal value) { currentDebt = value; }
    public BigDecimal getAvailableCredit() { return availableCredit; } public void setAvailableCredit(BigDecimal value) { availableCredit = value; }
    public LocalDate getNextClosingDate() { return nextClosingDate; } public void setNextClosingDate(LocalDate value) { nextClosingDate = value; }
    public LocalDate getNextPaymentDate() { return nextPaymentDate; } public void setNextPaymentDate(LocalDate value) { nextPaymentDate = value; }
    public Long getDaysUntilPayment() { return daysUntilPayment; } public void setDaysUntilPayment(Long value) { daysUntilPayment = value; }
    public PaymentAlertStatus getPaymentAlertStatus() { return paymentAlertStatus; } public void setPaymentAlertStatus(PaymentAlertStatus value) { paymentAlertStatus = value; }
}
