package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateFinancialTransactionRequest {
    @NotNull(message = "El tipo de movimiento es obligatorio.") private TransactionType type;
    private Long categoryId;
    private Long cardId;
    private Long paidCreditCardId;
    @NotNull(message = "El monto es obligatorio.") @Positive(message = "El monto debe ser mayor a cero.") @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;
    @NotBlank(message = "La moneda es obligatoria.") @Pattern(regexp = "[A-Za-z]{3}") private String currency;
    @Positive(message = "El tipo de cambio debe ser mayor a cero.") @Digits(integer = 13, fraction = 6)
    private BigDecimal exchangeRate;
    @Size(max = 500) private String description;
    @NotNull(message = "La fecha del movimiento es obligatoria.") private LocalDate transactionDate;

    public TransactionType getType() { return type; } public void setType(TransactionType value) { type = value; }
    public Long getCategoryId() { return categoryId; } public void setCategoryId(Long value) { categoryId = value; }
    public Long getCardId() { return cardId; } public void setCardId(Long value) { cardId = value; }
    public Long getPaidCreditCardId() { return paidCreditCardId; } public void setPaidCreditCardId(Long value) { paidCreditCardId = value; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal value) { amount = value; }
    public String getCurrency() { return currency; } public void setCurrency(String value) { currency = value; }
    public BigDecimal getExchangeRate() { return exchangeRate; } public void setExchangeRate(BigDecimal value) { exchangeRate = value; }
    public String getDescription() { return description; } public void setDescription(String value) { description = value; }
    public LocalDate getTransactionDate() { return transactionDate; } public void setTransactionDate(LocalDate value) { transactionDate = value; }
}
