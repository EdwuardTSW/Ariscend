package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.CreateFinancialTransactionRequest;
import com.ariscend.backend.dto.finance.FinanceSummaryResponse;
import com.ariscend.backend.dto.finance.FinancialTransactionResponse;
import com.ariscend.backend.dto.finance.UpdateFinancialTransactionRequest;
import com.ariscend.backend.entity.*;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialTransactionServiceTest {
    private static final Long USER_ID = 1L;
    @Mock FinancialTransactionRepository transactionRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock TransactionCategoryRepository categoryRepository;
    @Mock CardRepository cardRepository;
    @Mock FinanceSettingsService settingsService;
    @InjectMocks FinancialTransactionService service;

    @Test
    void createExpenseInBaseCurrencyStoresHistoricalValues() {
        AppUser user = user(); TransactionCategory category = category(CategoryType.EXPENSE);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(settingsService.getOrCreateEntity(USER_ID)).thenReturn(settings(user, "MXN"));
        when(categoryRepository.findAvailableById(3L, USER_ID)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0); transaction.setId(8L); transaction.assignTimestamps(); return transaction;
        });
        CreateFinancialTransactionRequest request = request(TransactionType.EXPENSE, "250.50"); request.setCategoryId(3L);

        FinancialTransactionResponse response = service.create(USER_ID, request);

        assertEquals(new BigDecimal("250.50"), response.getBaseAmount());
        assertEquals(new BigDecimal("1.000000"), response.getExchangeRate());
        assertEquals(TransactionType.EXPENSE, response.getType());
    }

    @Test
    void createCreditExpenseRejectsAmountAboveAvailableCredit() {
        AppUser user = user(); TransactionCategory category = category(CategoryType.EXPENSE); Card card = creditCard(user);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(settingsService.getOrCreateEntity(USER_ID)).thenReturn(settings(user, "MXN"));
        when(categoryRepository.findAvailableById(3L, USER_ID)).thenReturn(Optional.of(category));
        when(cardRepository.findOwnedForUpdate(4L, USER_ID)).thenReturn(Optional.of(card));
        when(transactionRepository.sumActiveByCardAndType(4L, TransactionType.EXPENSE)).thenReturn(new BigDecimal("900.00"));
        when(transactionRepository.sumActivePaymentsToCard(4L)).thenReturn(BigDecimal.ZERO);
        CreateFinancialTransactionRequest request = request(TransactionType.EXPENSE, "200.00");
        request.setCategoryId(3L); request.setCardId(4L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.create(USER_ID, request));

        assertEquals("La compra supera el crédito disponible.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createCardPaymentRejectsAmountAboveDebt() {
        AppUser user = user(); Card card = creditCard(user);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(settingsService.getOrCreateEntity(USER_ID)).thenReturn(settings(user, "MXN"));
        when(cardRepository.findOwnedForUpdate(4L, USER_ID)).thenReturn(Optional.of(card));
        when(transactionRepository.sumActiveByCardAndType(4L, TransactionType.EXPENSE)).thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.sumActivePaymentsToCard(4L)).thenReturn(new BigDecimal("100.00"));
        CreateFinancialTransactionRequest request = request(TransactionType.CREDIT_CARD_PAYMENT, "450.00");
        request.setPaidCreditCardId(4L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.create(USER_ID, request));

        assertEquals("El pago no puede superar la deuda actual.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void summaryExcludesCardPaymentsFromIncomeAndExpenses() {
        LocalDate from = LocalDate.of(2026, 8, 1); LocalDate to = LocalDate.of(2026, 8, 31);
        when(appUserRepository.existsById(USER_ID)).thenReturn(true);
        when(settingsService.getOrCreateEntity(USER_ID)).thenReturn(settings(user(), "MXN"));
        when(transactionRepository.summarizeByType(USER_ID, from, to)).thenReturn(List.of(
                new Object[]{TransactionType.INCOME, new BigDecimal("1000.00")},
                new Object[]{TransactionType.EXPENSE, new BigDecimal("400.00")}
        ));
        when(transactionRepository.summarizeByCategory(anyLong(), any(), any(), any())).thenReturn(List.of());
        when(transactionRepository.summarizeOriginalByCurrency(USER_ID, from, to)).thenReturn(List.of());

        FinanceSummaryResponse response = service.getSummary(USER_ID, from, to);

        assertEquals(new BigDecimal("600.00"), response.getBalance());
        assertEquals(new BigDecimal("400.00"), response.getTotalExpenses());
    }

    @Test
    void getByIdDoesNotReturnMovementOwnedByAnotherUser() {
        when(transactionRepository.findByIdAndUserId(9L, 2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById(2L, 9L));
        verify(transactionRepository, never()).save(any());
        verify(transactionRepository, never()).delete(any());
    }

    @Test
    void cancelCreditExpenseRejectsPaymentsAboveRemainingPurchases() {
        AppUser user = user(); Card card = creditCard(user);
        FinancialTransaction transaction = movement(user, card, TransactionType.EXPENSE, "500.00");
        when(transactionRepository.findByIdAndUserId(9L, USER_ID)).thenReturn(Optional.of(transaction));
        when(cardRepository.findOwnedForUpdate(4L, USER_ID)).thenReturn(Optional.of(card));
        when(transactionRepository.sumActiveByCardAndType(4L, TransactionType.EXPENSE)).thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.sumActivePaymentsToCard(4L)).thenReturn(new BigDecimal("300.00"));

        assertThrows(IllegalStateException.class, () -> service.cancel(USER_ID, 9L));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cancelCardPaymentRejectsDebtAboveCreditLimit() {
        AppUser user = user(); Card card = creditCard(user);
        FinancialTransaction transaction = movement(user, null, TransactionType.CREDIT_CARD_PAYMENT, "300.00");
        transaction.setPaidCreditCard(card);
        when(transactionRepository.findByIdAndUserId(9L, USER_ID)).thenReturn(Optional.of(transaction));
        when(cardRepository.findOwnedForUpdate(4L, USER_ID)).thenReturn(Optional.of(card));
        when(transactionRepository.sumActiveByCardAndType(4L, TransactionType.EXPENSE)).thenReturn(new BigDecimal("1200.00"));
        when(transactionRepository.sumActivePaymentsToCard(4L)).thenReturn(new BigDecimal("300.00"));

        assertThrows(IllegalStateException.class, () -> service.cancel(USER_ID, 9L));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void updateCreditExpenseUsesProjectedLedgerWithoutDoubleCountingNewAmount() {
        AppUser user = user(); Card card = creditCard(user); TransactionCategory category = category(CategoryType.EXPENSE);
        FinancialTransaction transaction = movement(user, card, TransactionType.EXPENSE, "100.00");
        transaction.setCategory(category); transaction.setCurrency("MXN");
        when(transactionRepository.findByIdAndUserId(9L, USER_ID)).thenReturn(Optional.of(transaction));
        when(cardRepository.findOwnedForUpdate(4L, USER_ID)).thenReturn(Optional.of(card));
        when(settingsService.getOrCreateEntity(USER_ID)).thenReturn(settings(user, "MXN"));
        when(categoryRepository.findAvailableById(3L, USER_ID)).thenReturn(Optional.of(category));
        when(transactionRepository.sumActiveByCardAndType(4L, TransactionType.EXPENSE)).thenReturn(new BigDecimal("900.00"));
        when(transactionRepository.sumActivePaymentsToCard(4L)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.saveAndFlush(transaction)).thenReturn(transaction);
        UpdateFinancialTransactionRequest request = updateRequest("150.00");

        FinancialTransactionResponse response = service.update(USER_ID, 9L, request);

        assertEquals(new BigDecimal("150.00"), response.getAmount());
        verify(transactionRepository).saveAndFlush(transaction);
    }

    @Test
    void updateCreditExpenseRejectsPaymentsAboveProjectedPurchases() {
        AppUser user = user(); Card card = creditCard(user); TransactionCategory category = category(CategoryType.EXPENSE);
        FinancialTransaction transaction = movement(user, card, TransactionType.EXPENSE, "500.00");
        transaction.setCategory(category); transaction.setCurrency("MXN");
        when(transactionRepository.findByIdAndUserId(9L, USER_ID)).thenReturn(Optional.of(transaction));
        when(cardRepository.findOwnedForUpdate(4L, USER_ID)).thenReturn(Optional.of(card));
        when(settingsService.getOrCreateEntity(USER_ID)).thenReturn(settings(user, "MXN"));
        when(categoryRepository.findAvailableById(3L, USER_ID)).thenReturn(Optional.of(category));
        when(transactionRepository.sumActiveByCardAndType(4L, TransactionType.EXPENSE)).thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.sumActivePaymentsToCard(4L)).thenReturn(new BigDecimal("400.00"));

        assertThrows(IllegalStateException.class, () -> service.update(USER_ID, 9L, updateRequest("300.00")));

        verify(transactionRepository, never()).saveAndFlush(any());
    }

    private CreateFinancialTransactionRequest request(TransactionType type, String amount) {
        CreateFinancialTransactionRequest request = new CreateFinancialTransactionRequest(); request.setType(type);
        request.setAmount(new BigDecimal(amount)); request.setCurrency("MXN"); request.setTransactionDate(LocalDate.of(2026, 8, 14)); return request;
    }
    private AppUser user() { AppUser user = new AppUser(); user.setId(USER_ID); return user; }
    private UpdateFinancialTransactionRequest updateRequest(String amount) {
        UpdateFinancialTransactionRequest request = new UpdateFinancialTransactionRequest();
        request.setType(TransactionType.EXPENSE); request.setCategoryId(3L); request.setCardId(4L);
        request.setAmount(new BigDecimal(amount)); request.setCurrency("MXN");
        request.setTransactionDate(LocalDate.of(2026, 8, 14)); return request;
    }
    private FinanceSettings settings(AppUser user, String currency) { FinanceSettings settings = new FinanceSettings(); settings.setUser(user); settings.setBaseCurrency(currency); return settings; }
    private TransactionCategory category(CategoryType type) { TransactionCategory category = new TransactionCategory(); category.setId(3L); category.setName("Compras"); category.setType(type); category.setSystemDefined(true); return category; }
    private Card creditCard(AppUser user) { Card card = new Card(); card.setId(4L); card.setUser(user); card.setType(CardType.CREDIT); card.setCurrency("MXN"); card.setCreditLimit(new BigDecimal("1000.00")); card.setActive(true); return card; }
    private FinancialTransaction movement(AppUser user, Card card, TransactionType type, String amount) {
        FinancialTransaction transaction = new FinancialTransaction(); transaction.setId(9L); transaction.setUser(user);
        transaction.setCard(card); transaction.setType(type); transaction.setAmount(new BigDecimal(amount));
        transaction.setStatus(FinancialStatus.ACTIVE); return transaction;
    }
}
