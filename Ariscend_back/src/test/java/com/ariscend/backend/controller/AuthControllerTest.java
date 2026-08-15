package com.ariscend.backend.controller;

import com.ariscend.backend.dto.auth.RegisterRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.service.AppUserService;
import com.ariscend.backend.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AppUserService appUserService = mock(AppUserService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final SecurityContextRepository contextRepository = mock(SecurityContextRepository.class);
    private final SessionAuthenticationStrategy sessionStrategy = mock(SessionAuthenticationStrategy.class);
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrations = mock(ObjectProvider.class);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerEstablishesAndPersistsAuthenticatedSession() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ana");
        request.setEmail("ana@example.com");
        request.setPassword("long-enough-password");
        AppUser user = new AppUser();
        user.setId(7L);
        user.setName("Ana");
        user.setEmail("ana@example.com");
        when(authService.register(request)).thenReturn(user);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        AuthController controller = new AuthController(
                authService, appUserService, authenticationManager, contextRepository, sessionStrategy, clientRegistrations);

        var response = controller.register(request, servletRequest, servletResponse);

        assertEquals(7L, response.getId());
        assertEquals("ana@example.com", response.getEmail());
        verify(sessionStrategy).onAuthentication(any(), any(), any());
        verify(contextRepository).saveContext(any(SecurityContext.class), any(), any());
        assertEquals("ana@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
