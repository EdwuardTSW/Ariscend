package com.ariscend.backend.dto.finance;

import com.ariscend.backend.entity.Card;
import com.ariscend.backend.entity.CardType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CardResponse {
    private Long id;
    private Long userId;
    private String alias;
    private String issuer;
    private CardType type;
    private String lastFourDigits;
    private String currency;
    private BigDecimal creditLimit;
    private BigDecimal openingBalance;
    private Integer closingDay;
    private Integer paymentDueDay;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    public static CardResponse from(Card card) {
        CardResponse response = new CardResponse();
        response.id = card.getId(); response.userId = card.getUser().getId();
        response.alias = card.getAlias(); response.issuer = card.getIssuer(); response.type = card.getType();
        response.lastFourDigits = card.getLastFourDigits(); response.currency = card.getCurrency();
        response.creditLimit = card.getCreditLimit(); response.openingBalance = card.getOpeningBalance();
        response.closingDay = card.getClosingDay(); response.paymentDueDay = card.getPaymentDueDay();
        response.active = card.isActive(); response.createdAt = card.getCreatedAt(); response.cancelledAt = card.getCancelledAt();
        return response;
    }
    public Long getId() { return id; } public Long getUserId() { return userId; }
    public String getAlias() { return alias; } public String getIssuer() { return issuer; }
    public CardType getType() { return type; } public String getLastFourDigits() { return lastFourDigits; }
    public String getCurrency() { return currency; } public BigDecimal getCreditLimit() { return creditLimit; }
    public BigDecimal getOpeningBalance() { return openingBalance; } public Integer getClosingDay() { return closingDay; }
    public Integer getPaymentDueDay() { return paymentDueDay; } public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getCancelledAt() { return cancelledAt; }
}
