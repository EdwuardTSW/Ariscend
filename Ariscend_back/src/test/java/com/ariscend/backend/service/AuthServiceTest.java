package com.ariscend.backend.service;

import com.ariscend.backend.dto.auth.RegisterRequest;
import com.ariscend.backend.dto.auth.ChangePasswordRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.FinanceSettings;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.FinanceSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AppUserRepository repository;
    @Mock FinanceSettingsRepository financeSettingsRepository;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void registerNormalizesFieldsAndHashesPassword() {
        RegisterRequest request = request("  Ana  ", "  ANA@EXAMPLE.COM  ", "long-enough-password");
        when(passwordEncoder.encode("long-enough-password")).thenReturn("hash");
        when(repository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser user = new AuthService(repository, financeSettingsRepository, passwordEncoder).register(request);

        assertEquals("Ana", user.getName());
        assertEquals("ana@example.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        verify(financeSettingsRepository).save(argThat(settings -> settings.getUser() == user
                && "MXN".equals(settings.getBaseCurrency())
                && settings.getPaymentAlertDays() == 3));
    }

    @Test
    void registerRejectsDuplicateEmailAsConflict() {
        RegisterRequest request = request("Ana", "ANA@example.com", "long-enough-password");
        when(repository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> new AuthService(repository, financeSettingsRepository, passwordEncoder).register(request));

        verify(repository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
        verify(financeSettingsRepository, never()).save(any(FinanceSettings.class));
    }

    @Test
    void changePasswordVerifiesCurrentPasswordAndStoresNewHash() {
        AppUser user = new AppUser();
        user.setEmail("ana@example.com");
        user.setPasswordHash("old-hash");
        ChangePasswordRequest request = changePasswordRequest("current-password", "new-secure-password");
        when(repository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-secure-password", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-secure-password")).thenReturn("new-hash");

        new AuthService(repository, financeSettingsRepository, passwordEncoder)
                .changePassword("ANA@EXAMPLE.COM", request);

        assertEquals("new-hash", user.getPasswordHash());
        verify(repository).save(user);
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        AppUser user = new AppUser();
        user.setPasswordHash("old-hash");
        ChangePasswordRequest request = changePasswordRequest("wrong-password", "new-secure-password");
        when(repository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new AuthService(repository, financeSettingsRepository, passwordEncoder)
                        .changePassword("ana@example.com", request));

        assertEquals("La contraseña actual no es correcta.", exception.getMessage());
        assertEquals("old-hash", user.getPasswordHash());
        verify(repository, never()).save(user);
        verify(passwordEncoder, never()).encode(any());
    }

    private RegisterRequest request(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private ChangePasswordRequest changePasswordRequest(String currentPassword, String newPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        return request;
    }
}
