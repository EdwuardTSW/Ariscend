package com.ariscend.backend.dto.finance;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGoalContributionRequest {
    @NotNull(message = "El monto es obligatorio.") @Positive(message = "El monto debe ser mayor a cero.") @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;
    @Positive(message = "El tipo de cambio debe ser mayor a cero.") @Digits(integer = 13, fraction = 6)
    private BigDecimal exchangeRate;
    @NotNull(message = "La fecha del aporte es obligatoria.") private LocalDate contributionDate;
    @Size(max = 500) private String notes;
    private Long sourceDebitCardId;
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal value) { amount = value; }
    public BigDecimal getExchangeRate() { return exchangeRate; } public void setExchangeRate(BigDecimal value) { exchangeRate = value; }
    public LocalDate getContributionDate() { return contributionDate; } public void setContributionDate(LocalDate value) { contributionDate = value; }
    public String getNotes() { return notes; } public void setNotes(String value) { notes = value; }
    public Long getSourceDebitCardId() { return sourceDebitCardId; } public void setSourceDebitCardId(Long value) { sourceDebitCardId = value; }
}
