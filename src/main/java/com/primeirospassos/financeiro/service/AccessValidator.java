package com.primeirospassos.financeiro.service;

import com.primeirospassos.financeiro.integration.AlunosClient;
import com.primeirospassos.financeiro.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessValidator {

    private final AlunosClient alunosClient;

    public void validateAccess(String resourceEscolaId, CurrentUser user, Long alunoId, boolean writeOperation) {
        validateUserContext(user);

        if (!resourceEscolaId.equals(user.escolaId())) {
            throw new AccessDeniedException("escolaId não autorizado");
        }

        if (user.isAdmin()) {
            return;
        }

        if (writeOperation) {
            throw new AccessDeniedException("RESPONSAVEL sem permissão de escrita");
        }

        if (alunoId != null && !alunosClient.alunoPertenceAoResponsavel(alunoId, user.token())) {
            throw new AccessDeniedException("Aluno não pertence ao responsável");
        }
    }

    public void validateAdmin(CurrentUser user) {
        validateUserContext(user);
        if (!user.isAdmin()) {
            throw new AccessDeniedException("Apenas ADMIN pode executar essa operação");
        }
    }

    public void validateUserContext(CurrentUser user) {
        if (user == null || user.userId() == null) {
            throw new AccessDeniedException("Usuário inválido");
        }
        if (user.escolaId() == null || user.escolaId().isBlank()) {
            throw new AccessDeniedException("escolaId obrigatório");
        }
        if (!user.isAdmin() && !user.isResponsavel()) {
            throw new AccessDeniedException("Role não autorizada");
        }
    }
}
