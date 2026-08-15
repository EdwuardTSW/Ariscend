package com.ariscend.backend.security;

import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserOwnershipFilterTest {

    private final AppUserRepository repository = mock(AppUserRepository.class);
    private final FilterChain chain = mock(FilterChain.class);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsAnotherUsersNumericPath() throws Exception {
        authenticate("ana@example.com");
        AppUser user = user(7L, "ana@example.com");
        when(repository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(user));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/8/tasks");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new UserOwnershipFilter(repository).doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void allowsTheAuthenticatedUsersPath() throws Exception {
        authenticate("ana@example.com");
        when(repository.findByEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(user(7L, "ana@example.com")));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/7/tasks");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new UserOwnershipFilter(repository).doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(email, null, List.of()));
    }

    private AppUser user(long id, String email) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        return user;
    }
}
