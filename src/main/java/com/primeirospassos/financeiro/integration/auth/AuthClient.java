package com.primeirospassos.financeiro.integration.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClient {

    private final WebClient authWebClient;

    public SessionResponse validarSessao(String sessionId, String authorization) {
        return authWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/auth/session").queryParam("sessionId", sessionId).build())
                .header("Authorization", authorization)
                .header("X-Session-Id", sessionId)
                .retrieve()
                .bodyToMono(SessionResponse.class)
                .doOnError(error -> log.warn("Falha ao validar sessão {}", sessionId, error))
                .block();
    }
}
