package com.ariscend.backend.service;

import com.ariscend.backend.dto.user.CreateUserRequest;
import com.ariscend.backend.dto.user.UserResponse;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {
    @Mock AppUserRepository repository;
    @InjectMocks AppUserService service;

    @Test
    void createNormalizesNameAndEmail() {
        CreateUserRequest request = new CreateUserRequest(); request.setName("  Ana  "); request.setEmail("  ANA@EXAMPLE.COM  ");
        when(repository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
        when(repository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0); user.setId(1L); return user;
        });

        UserResponse response = service.create(request);

        assertEquals("Ana", response.getName());
        assertEquals("ana@example.com", response.getEmail());
    }

    @Test
    void createRejectsDuplicateEmailIgnoringCase() {
        CreateUserRequest request = new CreateUserRequest(); request.setName("Ana"); request.setEmail("ANA@example.com");
        when(repository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(request));

        verify(repository, never()).save(any());
    }
}
