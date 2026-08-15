package com.ariscend.backend.security;

import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.service.AuthService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class EmailOidcUserService extends OidcUserService {

    private final AppUserRepository appUserRepository;
    private final AuthService authService;

    public EmailOidcUserService(AppUserRepository appUserRepository, AuthService authService) {
        this.appUserRepository = appUserRepository;
        this.authService = authService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        if (!Boolean.TRUE.equals(oidcUser.getEmailVerified()) || oidcUser.getEmail() == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unverified_email"), "Google must provide a verified email address");
        }

        String email = AuthService.normalizeEmail(oidcUser.getEmail());
        appUserRepository.findByEmailIgnoreCase(email).orElseGet(() ->
                authService.provisionOidcUser(oidcUser.getFullName(), email));
        return new DefaultOidcUser(
                oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(), "email");
    }

}
