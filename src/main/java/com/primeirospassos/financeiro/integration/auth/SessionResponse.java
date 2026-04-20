package com.primeirospassos.financeiro.integration.auth;

import lombok.Data;

@Data
public class SessionResponse {
    private Long userId;
    private String escolaId;
    private String role;
}
