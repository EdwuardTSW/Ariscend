package com.ariscend.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

final class FinanceUtils {

    private FinanceUtils() {
    }

    static String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("La moneda es obligatoria.");
        }
        String normalized = currency.trim().toUpperCase();
        try {
            Currency.getInstance(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("La moneda no es válida.");
        }
        return normalized;
    }

    static BigDecimal calculateBaseAmount(
            BigDecimal amount,
            String currency,
            String baseCurrency,
            BigDecimal exchangeRate
    ) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (currency.equals(baseCurrency)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
        if (exchangeRate == null || exchangeRate.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El tipo de cambio debe ser mayor a cero cuando la moneda es diferente."
            );
        }
        BigDecimal baseAmount = amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        if (baseAmount.precision() > 19) {
            throw new IllegalArgumentException("El monto convertido es demasiado grande.");
        }
        return baseAmount;
    }

    static BigDecimal normalizedExchangeRate(
            String currency,
            String baseCurrency,
            BigDecimal exchangeRate
    ) {
        if (currency.equals(baseCurrency)) {
            return BigDecimal.ONE.setScale(6, RoundingMode.UNNECESSARY);
        }
        return exchangeRate.setScale(6, RoundingMode.HALF_UP);
    }
}
