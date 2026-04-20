package com.primeirospassos.financeiro.security;

public record CurrentUser(Long userId, String escolaId, String role, String token, String sessionId) {

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isResponsavel() {
        return "RESPONSAVEL".equalsIgnoreCase(role);
    }
}
