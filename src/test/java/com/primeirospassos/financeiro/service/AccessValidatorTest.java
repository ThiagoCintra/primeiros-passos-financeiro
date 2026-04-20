package com.primeirospassos.financeiro.service;

import com.primeirospassos.financeiro.integration.AlunosClient;
import com.primeirospassos.financeiro.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessValidatorTest {

    @Mock
    private AlunosClient alunosClient;

    @InjectMocks
    private AccessValidator accessValidator;

    @Test
    void shouldAllowAdminWrite() {
        CurrentUser user = new CurrentUser("esc-1", "ADMIN", "Bearer token");

        assertDoesNotThrow(() -> accessValidator.validateAccess("esc-1", user, 10L, true));
    }

    @Test
    void shouldBlockResponsavelWrite() {
        CurrentUser user = new CurrentUser("esc-1", "RESPONSAVEL", "Bearer token");

        assertThrows(AccessDeniedException.class, () -> accessValidator.validateAccess("esc-1", user, 10L, true));
    }

    @Test
    void shouldValidateOwnershipForResponsavelRead() {
        CurrentUser user = new CurrentUser("esc-1", "RESPONSAVEL", "Bearer token");
        when(alunosClient.alunoPertenceAoResponsavel(10L, "Bearer token")).thenReturn(true);

        assertDoesNotThrow(() -> accessValidator.validateAccess("esc-1", user, 10L, false));
    }

    @Test
    void shouldBlockDifferentEscolaId() {
        CurrentUser user = new CurrentUser("esc-2", "ADMIN", "Bearer token");

        assertThrows(AccessDeniedException.class, () -> accessValidator.validateAccess("esc-1", user, 10L, false));
    }
}
