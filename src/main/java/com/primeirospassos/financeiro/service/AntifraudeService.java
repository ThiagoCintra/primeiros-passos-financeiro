package com.primeirospassos.financeiro.service;

import com.primeirospassos.financeiro.dto.CreatePagamentoRequest;
import com.primeirospassos.financeiro.entity.AntifraudeRegistro;
import com.primeirospassos.financeiro.entity.Pagamento;
import com.primeirospassos.financeiro.repository.AntifraudeRegistroRepository;
import com.primeirospassos.financeiro.repository.PagamentoRepository;
import com.primeirospassos.financeiro.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AntifraudeService {

    private final PagamentoRepository pagamentoRepository;
    private final AntifraudeRegistroRepository antifraudeRegistroRepository;

    @Value("${antifraude.valor-alto-limite:5000}")
    private BigDecimal valorAltoLimite;

    @Value("${antifraude.max-tentativas:3}")
    private long maxTentativas;

    @Value("${antifraude.janela-tentativas-minutos:10}")
    private long janelaTentativasMinutos;

    @Value("${antifraude.janela-duplicidade-minutos:5}")
    private long janelaDuplicidadeMinutos;

    public Optional<String> avaliar(CreatePagamentoRequest request, CurrentUser user) {
        if (request.getValor().compareTo(valorAltoLimite) >= 0) {
            return Optional.of("valor alto");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioJanelaTentativa = agora.minusMinutes(janelaTentativasMinutos);
        long tentativas = pagamentoRepository.countByEscolaIdAndAlunoIdAndDataPagamentoAfter(
                user.escolaId(), request.getAlunoId(), inicioJanelaTentativa);
        if (tentativas >= maxTentativas) {
            return Optional.of("múltiplas tentativas");
        }

        LocalDateTime inicioJanelaDuplicidade = agora.minusMinutes(janelaDuplicidadeMinutos);
        boolean duplicidade = pagamentoRepository.existsByEscolaIdAndAlunoIdAndValorAndMetodoPagamentoAndDataPagamentoAfter(
                user.escolaId(), request.getAlunoId(), request.getValor(), request.getMetodoPagamento(), inicioJanelaDuplicidade);
        if (duplicidade) {
            return Optional.of("duplicidade");
        }

        return Optional.empty();
    }

    public void registrarSuspeita(Pagamento pagamento, String motivo) {
        AntifraudeRegistro registro = new AntifraudeRegistro();
        registro.setPagamentoPublicId(pagamento.getPublicId());
        registro.setEscolaId(pagamento.getEscolaId());
        registro.setAlunoId(pagamento.getAlunoId());
        registro.setMotivo(motivo);
        antifraudeRegistroRepository.save(registro);
    }
}
