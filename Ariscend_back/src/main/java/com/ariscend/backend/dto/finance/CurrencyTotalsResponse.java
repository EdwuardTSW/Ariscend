package com.ariscend.backend.dto.finance;

import java.math.BigDecimal;

public class CurrencyTotalsResponse {
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expenses = BigDecimal.ZERO;
    public BigDecimal getIncome() { return income; } public void setIncome(BigDecimal value) { income = value; }
    public BigDecimal getExpenses() { return expenses; } public void setExpenses(BigDecimal value) { expenses = value; }
}
