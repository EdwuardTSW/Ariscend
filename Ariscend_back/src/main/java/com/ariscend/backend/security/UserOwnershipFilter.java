package com.ariscend.backend.security;

import com.ariscend.backend.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserOwnershipFilter extends OncePerRequestFilter {

    private static final Pattern USER_PATH = Pattern.compile("^/api/users/(\\d+)(?:/.*)?$");
    private final AppUserRepository appUserRepository;

    public UserOwnershipFilter(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Matcher matcher = USER_PATH.matcher(request.getRequestURI().substring(request.getContextPath().length()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (matcher.matches() && isAuthenticated(authentication)) {
            BigInteger requestedUserId = new BigInteger(matcher.group(1));
            boolean ownsPath = appUserRepository.findByEmailIgnoreCase(authentication.getName())
                    .map(user -> BigInteger.valueOf(user.getId()).equals(requestedUserId))
                    .orElse(false);
            if (!ownsPath) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"message\":\"No tienes permiso para acceder a este usuario.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
