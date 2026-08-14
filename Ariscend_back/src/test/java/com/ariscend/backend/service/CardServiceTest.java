package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.CardSummaryResponse;
import com.ariscend.backend.entity.*;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.CardRepository;
import com.ariscend.backend.repository.FinancialTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock CardRepository cardRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock FinancialTransactionRepository transactionRepository;
    @Mock FinanceSettingsService settingsService;
    @InjectMocks CardService service;

    @Test
    void debitSummaryUsesOpeningBalanceAndMovements() {
        AppUser user = new AppUser(); user.setId(1L);
        Card card = new Card(); card.setId(2L); card.setUser(user); card.setType(CardType.DEBIT);
        card.setCurrency("MXN"); card.setOpeningBalance(new BigDecimal("1000.00"));
        when(cardRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(card));
        when(transactionRepository.sumActiveByCardAndType(2L, TransactionType.INCOME)).thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.sumActiveByCardAndType(2L, TransactionType.EXPENSE)).thenReturn(new BigDecimal("200.00"));
        when(transactionRepository.sumActiveByCardAndType(2L, TransactionType.CREDIT_CARD_PAYMENT)).thenReturn(new BigDecimal("100.00"));
        when(transactionRepository.sumActivePaymentsToCard(2L)).thenReturn(BigDecimal.ZERO);

        CardSummaryResponse response = service.getSummary(1L, 2L);

        assertEquals(new BigDecimal("1200.00"), response.getCurrentBalance());
        assertEquals(PaymentAlertStatus.NONE, response.getPaymentAlertStatus());
    }
}
