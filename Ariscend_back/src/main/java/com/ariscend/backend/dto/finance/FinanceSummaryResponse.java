package com.ariscend.backend.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class FinanceSummaryResponse {
    private String baseCurrency;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private Map<String, BigDecimal> incomeByCategory;
    private Map<String, BigDecimal> expensesByCategory;
    private Map<String, CurrencyTotalsResponse> originalTotalsByCurrency;

    public String getBaseCurrency() { return baseCurrency; } public void setBaseCurrency(String value) { baseCurrency = value; }
    public LocalDate getDateFrom() { return dateFrom; } public void setDateFrom(LocalDate value) { dateFrom = value; }
    public LocalDate getDateTo() { return dateTo; } public void setDateTo(LocalDate value) { dateTo = value; }
    public BigDecimal getTotalIncome() { return totalIncome; } public void setTotalIncome(BigDecimal value) { totalIncome = value; }
    public BigDecimal getTotalExpenses() { return totalExpenses; } public void setTotalExpenses(BigDecimal value) { totalExpenses = value; }
    public BigDecimal getBalance() { return balance; } public void setBalance(BigDecimal value) { balance = value; }
    public Map<String, BigDecimal> getIncomeByCategory() { return incomeByCategory; } public void setIncomeByCategory(Map<String, BigDecimal> value) { incomeByCategory = value; }
    public Map<String, BigDecimal> getExpensesByCategory() { return expensesByCategory; } public void setExpensesByCategory(Map<String, BigDecimal> value) { expensesByCategory = value; }
    public Map<String, CurrencyTotalsResponse> getOriginalTotalsByCurrency() { return originalTotalsByCurrency; } public void setOriginalTotalsByCurrency(Map<String, CurrencyTotalsResponse> value) { originalTotalsByCurrency = value; }
}
