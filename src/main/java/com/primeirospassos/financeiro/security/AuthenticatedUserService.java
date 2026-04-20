package com.primeirospassos.financeiro.security;

import com.primeirospassos.financeiro.integration.auth.AuthClient;
import com.primeirospassos.financeiro.integration.auth.SessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
@Slf4j
public class AuthenticatedUserService {

    private static final String USER_CACHE_KEY = AuthenticatedUserService.class.getName() + ".CURRENT_USER";

    private final AuthClient authClient;
    private final SessionCacheService sessionCacheService;

    public AuthenticatedUserService(AuthClient authClient, SessionCacheService sessionCacheService) {
        this.authClient = authClient;
        this.sessionCacheService = sessionCacheService;
    }

    public CurrentUser currentUser() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            Object cached = requestAttributes.getAttribute(USER_CACHE_KEY, RequestAttributes.SCOPE_REQUEST);
            if (cached instanceof CurrentUser user) {
                return user;
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("JWT inválido");
        }

        String sessionId = asString(jwt.getClaim("sessionId"));
        if (sessionId == null || sessionId.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Claim sessionId obrigatória");
        }

        String token = "Bearer " + jwt.getTokenValue();
        SessionResponse session = sessionCacheService.getValid(sessionId)
                .orElseGet(() -> loadSessionFromLoginService(sessionId, token));

        CurrentUser user = new CurrentUser(session.getUserId(), session.getEscolaId(), normalizeRole(session.getRole()), token, sessionId);
        if (user.userId() == null || user.escolaId() == null || user.escolaId().isBlank() || user.role() == null || user.role().isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Sessão inválida");
        }

        if (requestAttributes != null) {
            requestAttributes.setAttribute(USER_CACHE_KEY, user, RequestAttributes.SCOPE_REQUEST);
        }
        return user;
    }

    private SessionResponse loadSessionFromLoginService(String sessionId, String token) {
        try {
            SessionResponse sessionResponse = authClient.validarSessao(sessionId, token);
            if (sessionResponse == null) {
                throw new AuthenticationCredentialsNotFoundException("Sessão não encontrada");
            }
            sessionCacheService.put(sessionId, sessionResponse);
            return sessionResponse;
        } catch (RuntimeException ex) {
            return sessionCacheService.getAny(sessionId)
                    .map(cached -> {
                        log.warn("Login-service indisponível, usando sessão em cache para sessionId={}", sessionId);
                        return cached;
                    })
                    .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Não foi possível validar sessão", ex));
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String normalizeRole(String role) {
        return role == null ? null : role.trim().toUpperCase();
    }
}
