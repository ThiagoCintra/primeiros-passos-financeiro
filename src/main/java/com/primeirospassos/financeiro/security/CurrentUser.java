package com.primeirospassos.financeiro.security;

public record CurrentUser(String escolaId, String role, String token) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isResponsavel() {
        return "RESPONSAVEL".equalsIgnoreCase(role);
    }
}
