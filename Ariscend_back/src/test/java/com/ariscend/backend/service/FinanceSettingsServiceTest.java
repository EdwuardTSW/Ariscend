package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.FinanceSettingsResponse;
import com.ariscend.backend.dto.finance.UpdateFinanceSettingsRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.FinanceSettings;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.FinanceSettingsRepository;
import com.ariscend.backend.repository.FinancialTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceSettingsServiceTest {
    @Mock FinanceSettingsRepository settingsRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock FinancialTransactionRepository transactionRepository;
    @InjectMocks FinanceSettingsService service;

    @Test
    void getCreatesDefaultMxnSettingsForExistingUser() {
        AppUser user = new AppUser(); user.setId(1L);
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(settingsRepository.save(any(FinanceSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinanceSettingsResponse response = service.get(1L);

        assertEquals("MXN", response.getBaseCurrency());
        assertEquals(3, response.getPaymentAlertDays());
    }

    @Test
    void updateRejectsBaseCurrencyChangeAfterMovementsExist() {
        AppUser user = new AppUser(); user.setId(1L);
        FinanceSettings settings = new FinanceSettings(); settings.setUser(user); settings.setBaseCurrency("MXN");
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.of(settings));
        when(transactionRepository.existsByUserId(1L)).thenReturn(true);
        UpdateFinanceSettingsRequest request = new UpdateFinanceSettingsRequest(); request.setBaseCurrency("USD");

        assertThrows(IllegalStateException.class, () -> service.update(1L, request));

        verify(settingsRepository, never()).saveAndFlush(any());
    }
}
