package com.ariscend.backend.service;

import com.ariscend.backend.dto.auth.RegisterRequest;
import com.ariscend.backend.dto.auth.ChangePasswordRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.FinanceSettings;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.FinanceSettingsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final FinanceSettingsRepository financeSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepository appUserRepository,
            FinanceSettingsRepository financeSettingsRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.financeSettingsRepository = financeSettingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Ese correo ya está registrado.");
        }

        AppUser user = new AppUser();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return saveWithDefaults(user);
    }

    @Transactional
    public AppUser provisionOidcUser(String name, String email) {
        AppUser user = new AppUser();
        user.setName(name == null || name.isBlank() ? email : name.trim());
        user.setEmail(normalizeEmail(email));
        return saveWithDefaults(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalStateException("La cuenta autenticada ya no existe."));
        if (user.getPasswordHash() == null) {
            throw new IllegalStateException("Esta cuenta utiliza Google y no tiene una contraseña local.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente de la actual.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);
    }

    private AppUser saveWithDefaults(AppUser user) {
        AppUser savedUser = appUserRepository.save(user);
        FinanceSettings settings = new FinanceSettings();
        settings.setUser(savedUser);
        financeSettingsRepository.save(settings);
        return savedUser;
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
