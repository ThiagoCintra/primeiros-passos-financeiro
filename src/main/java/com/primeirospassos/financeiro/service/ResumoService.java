package com.primeirospassos.financeiro.service;

import com.primeirospassos.financeiro.dto.ResumoResponse;
import com.primeirospassos.financeiro.enumtype.DebitoStatus;
import com.primeirospassos.financeiro.enumtype.MovimentacaoTipo;
import com.primeirospassos.financeiro.enumtype.PagamentoStatus;
import com.primeirospassos.financeiro.integration.AlunosClient;
import com.primeirospassos.financeiro.repository.DebitoAlunoRepository;
import com.primeirospassos.financeiro.repository.MovimentacaoRepository;
import com.primeirospassos.financeiro.repository.PagamentoRepository;
import com.primeirospassos.financeiro.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final DebitoAlunoRepository debitoAlunoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final AlunosClient alunosClient;
    private final AccessValidator accessValidator;

    @Transactional(readOnly = true)
    public ResumoResponse gerar(CurrentUser user) {
        accessValidator.validateUserContext(user);

        if (user.isAdmin()) {
            return ResumoResponse.builder()
                    .totalEntradas(movimentacaoRepository.sumByEscolaAndTipo(user.escolaId(), MovimentacaoTipo.ENTRADA))
                    .totalSaidas(movimentacaoRepository.sumByEscolaAndTipo(user.escolaId(), MovimentacaoTipo.SAIDA))
                    .debitosPendentes(debitoAlunoRepository.sumByEscolaAndStatus(user.escolaId(), DebitoStatus.PENDENTE))
                    .pagamentosConfirmados(pagamentoRepository.sumByEscolaAndStatus(user.escolaId(), PagamentoStatus.CONFIRMADO))
                    .build();
        }

        if (user.isResponsavel()) {
            List<Long> alunos = alunosClient.listarAlunosDoResponsavel(user.token());
            if (alunos.isEmpty()) {
                return empty();
            }
            return ResumoResponse.builder()
                    .totalEntradas(movimentacaoRepository.sumByEscolaAndTipoAndAlunoIdIn(user.escolaId(), MovimentacaoTipo.ENTRADA, alunos))
                    .totalSaidas(movimentacaoRepository.sumByEscolaAndTipoAndAlunoIdIn(user.escolaId(), MovimentacaoTipo.SAIDA, alunos))
                    .debitosPendentes(debitoAlunoRepository.sumByEscolaAndStatusAndAlunoIdIn(user.escolaId(), DebitoStatus.PENDENTE, alunos))
                    .pagamentosConfirmados(pagamentoRepository.sumByEscolaAndStatusAndAlunoIdIn(user.escolaId(), PagamentoStatus.CONFIRMADO, alunos))
                    .build();
        }

        return empty();
    }

    private ResumoResponse empty() {
        return ResumoResponse.builder()
                .totalEntradas(BigDecimal.ZERO)
                .totalSaidas(BigDecimal.ZERO)
                .debitosPendentes(BigDecimal.ZERO)
                .pagamentosConfirmados(BigDecimal.ZERO)
                .build();
    }
}
