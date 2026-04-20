package com.primeirospassos.financeiro.service;

import com.primeirospassos.financeiro.dto.DebitoResponse;
import com.primeirospassos.financeiro.entity.DebitoAluno;
import com.primeirospassos.financeiro.integration.AlunosClient;
import com.primeirospassos.financeiro.mapper.FinanceiroMapper;
import com.primeirospassos.financeiro.repository.DebitoAlunoRepository;
import com.primeirospassos.financeiro.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebitoService {

    private final DebitoAlunoRepository debitoAlunoRepository;
    private final FinanceiroMapper mapper;
    private final AlunosClient alunosClient;

    @Transactional(readOnly = true)
    public List<DebitoResponse> listar(CurrentUser user) {
        List<DebitoAluno> debitos;
        if (user.isAdmin()) {
            debitos = debitoAlunoRepository.findAllByEscolaId(user.escolaId());
        } else if (user.isResponsavel()) {
            List<Long> alunos = alunosClient.listarAlunosDoResponsavel(user.token());
            if (alunos.isEmpty()) {
                return Collections.emptyList();
            }
            debitos = debitoAlunoRepository.findAllByEscolaIdAndAlunoIdIn(user.escolaId(), alunos);
        } else {
            debitos = Collections.emptyList();
        }
        return mapper.toDebitoResponses(debitos);
    }
}
