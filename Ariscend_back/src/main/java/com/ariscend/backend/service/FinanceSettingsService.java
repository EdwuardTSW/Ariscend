package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.FinanceSettingsResponse;
import com.ariscend.backend.dto.finance.UpdateFinanceSettingsRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.FinanceSettings;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.FinanceSettingsRepository;
import com.ariscend.backend.repository.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FinanceSettingsService {

    private final FinanceSettingsRepository settingsRepository;
    private final AppUserRepository appUserRepository;
    private final FinancialTransactionRepository transactionRepository;

    public FinanceSettingsService(
            FinanceSettingsRepository settingsRepository,
            AppUserRepository appUserRepository,
            FinancialTransactionRepository transactionRepository
    ) {
        this.settingsRepository = settingsRepository;
        this.appUserRepository = appUserRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public FinanceSettingsResponse get(Long userId) {
        return FinanceSettingsResponse.from(getOrCreateEntity(userId));
    }

    @Transactional
    public FinanceSettingsResponse update(Long userId, UpdateFinanceSettingsRequest request) {
        FinanceSettings settings = getOrCreateEntity(userId);
        String newCurrency = FinanceUtils.normalizeCurrency(request.getBaseCurrency());
        if (!settings.getBaseCurrency().equals(newCurrency) && transactionRepository.existsByUserId(userId)) {
            throw new IllegalStateException("No se puede cambiar la moneda base después de registrar movimientos.");
        }
        settings.setBaseCurrency(newCurrency);
        settings.setPaymentAlertDays(
                request.getPaymentAlertDays() == null ? 3 : request.getPaymentAlertDays()
        );
        return FinanceSettingsResponse.from(settingsRepository.saveAndFlush(settings));
    }

    @Transactional
    public FinanceSettings getOrCreateEntity(Long userId) {
        return settingsRepository.findByUserId(userId).orElseGet(() -> {
            AppUser user = appUserRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
            FinanceSettings settings = new FinanceSettings();
            settings.setUser(user);
            return settingsRepository.save(settings);
        });
    }
}
