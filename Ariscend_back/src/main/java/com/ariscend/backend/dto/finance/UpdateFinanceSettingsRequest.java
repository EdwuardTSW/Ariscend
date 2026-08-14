package com.ariscend.backend.dto.finance;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateFinanceSettingsRequest {

    @NotBlank(message = "La moneda base es obligatoria.")
    @Pattern(regexp = "[A-Za-z]{3}", message = "La moneda base debe tener tres letras.")
    private String baseCurrency;

    @Min(value = 0, message = "Los días de alerta no pueden ser negativos.")
    @Max(value = 30, message = "Los días de alerta no pueden superar 30.")
    private Integer paymentAlertDays = 3;

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public Integer getPaymentAlertDays() { return paymentAlertDays; }
    public void setPaymentAlertDays(Integer paymentAlertDays) { this.paymentAlertDays = paymentAlertDays; }
}
