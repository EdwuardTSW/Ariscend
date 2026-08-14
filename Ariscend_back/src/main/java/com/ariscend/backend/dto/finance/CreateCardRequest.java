package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.CardType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateCardRequest {
    @NotBlank(message = "El alias de la tarjeta es obligatorio.") @Size(max = 80)
    private String alias;
    @NotBlank(message = "El emisor es obligatorio.") @Size(max = 80)
    private String issuer;
    @NotNull(message = "El tipo de tarjeta es obligatorio.")
    private CardType type;
    @NotBlank(message = "Los últimos cuatro dígitos son obligatorios.")
    @Pattern(regexp = "\\d{4}", message = "Deben indicarse exactamente cuatro dígitos.")
    private String lastFourDigits;
    @NotBlank(message = "La moneda es obligatoria.") @Pattern(regexp = "[A-Za-z]{3}")
    private String currency;
    @Positive(message = "El límite de crédito debe ser mayor a cero.") @Digits(integer = 17, fraction = 2)
    private BigDecimal creditLimit;
    @PositiveOrZero(message = "El saldo inicial no puede ser negativo.") @Digits(integer = 17, fraction = 2)
    private BigDecimal openingBalance;
    @Min(1) @Max(31) private Integer closingDay;
    @Min(1) @Max(31) private Integer paymentDueDay;

    public String getAlias() { return alias; } public void setAlias(String alias) { this.alias = alias; }
    public String getIssuer() { return issuer; } public void setIssuer(String issuer) { this.issuer = issuer; }
    public CardType getType() { return type; } public void setType(CardType type) { this.type = type; }
    public String getLastFourDigits() { return lastFourDigits; } public void setLastFourDigits(String value) { this.lastFourDigits = value; }
    public String getCurrency() { return currency; } public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getCreditLimit() { return creditLimit; } public void setCreditLimit(BigDecimal value) { this.creditLimit = value; }
    public BigDecimal getOpeningBalance() { return openingBalance; } public void setOpeningBalance(BigDecimal value) { this.openingBalance = value; }
    public Integer getClosingDay() { return closingDay; } public void setClosingDay(Integer value) { this.closingDay = value; }
    public Integer getPaymentDueDay() { return paymentDueDay; } public void setPaymentDueDay(Integer value) { this.paymentDueDay = value; }
}
