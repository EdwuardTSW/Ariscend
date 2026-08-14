package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.FinanceSettings;

public class FinanceSettingsResponse {

    private Long userId;
    private String baseCurrency;
    private Integer paymentAlertDays;

    public static FinanceSettingsResponse from(FinanceSettings settings) {
        FinanceSettingsResponse response = new FinanceSettingsResponse();
        response.userId = settings.getUser().getId();
        response.baseCurrency = settings.getBaseCurrency();
        response.paymentAlertDays = settings.getPaymentAlertDays();
        return response;
    }

    public Long getUserId() { return userId; }
    public String getBaseCurrency() { return baseCurrency; }
    public Integer getPaymentAlertDays() { return paymentAlertDays; }
}
