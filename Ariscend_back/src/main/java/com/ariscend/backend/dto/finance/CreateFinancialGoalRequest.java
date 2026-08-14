package com.ariscend.backend.dto.finance;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateFinancialGoalRequest {
    @NotBlank(message = "El nombre de la meta es obligatorio.") @Size(max = 120) private String name;
    @Size(max = 500) private String description;
    @NotNull(message = "El monto objetivo es obligatorio.") @Positive(message = "El monto objetivo debe ser mayor a cero.") @Digits(integer = 17, fraction = 2)
    private BigDecimal targetAmount;
    @NotBlank(message = "La moneda es obligatoria.") @Pattern(regexp = "[A-Za-z]{3}") private String currency;
    private LocalDate targetDate;
    public String getName() { return name; } public void setName(String value) { name = value; }
    public String getDescription() { return description; } public void setDescription(String value) { description = value; }
    public BigDecimal getTargetAmount() { return targetAmount; } public void setTargetAmount(BigDecimal value) { targetAmount = value; }
    public String getCurrency() { return currency; } public void setCurrency(String value) { currency = value; }
    public LocalDate getTargetDate() { return targetDate; } public void setTargetDate(LocalDate value) { targetDate = value; }
}
