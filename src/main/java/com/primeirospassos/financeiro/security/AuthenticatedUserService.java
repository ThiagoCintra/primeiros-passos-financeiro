package com.primeirospassos.financeiro.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserService {

    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("JWT inválido");
        }

        String escolaId = asString(jwt.getClaim("escolaId"));
        if (escolaId == null || escolaId.isBlank()) {
            throw new AccessDeniedException("Claim escolaId obrigatória");
        }

        String role = asString(jwt.getClaim("role"));
        if (role == null || role.isBlank()) {
            throw new AccessDeniedException("Claim role obrigatória");
        }

        String token = "Bearer " + jwt.getTokenValue();
        return new CurrentUser(escolaId, role, token);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
