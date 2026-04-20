package com.primeirospassos.financeiro.service;

import com.primeirospassos.financeiro.dto.CreatePagamentoRequest;
import com.primeirospassos.financeiro.dto.PagamentoResponse;
import com.primeirospassos.financeiro.dto.WebhookRequest;
import com.primeirospassos.financeiro.entity.Pagamento;
import com.primeirospassos.financeiro.enumtype.PagamentoStatus;
import com.primeirospassos.financeiro.integration.AlunosClient;
import com.primeirospassos.financeiro.mapper.FinanceiroMapper;
import com.primeirospassos.financeiro.payment.PaymentProvider;
import com.primeirospassos.financeiro.payment.PaymentProviderRegistry;
import com.primeirospassos.financeiro.payment.PaymentProviderResult;
import com.primeirospassos.financeiro.repository.PagamentoRepository;
import com.primeirospassos.financeiro.security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PaymentProviderRegistry paymentProviderRegistry;
    private final FinanceiroMapper mapper;
    private final AccessValidator accessValidator;
    private final AlunosClient alunosClient;

    @Transactional
    public PagamentoResponse criar(CreatePagamentoRequest request, CurrentUser user) {
        accessValidator.validateAdmin(user);
        alunosClient.buscarAlunoPorId(request.getAlunoId(), user.token())
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado"));

        PaymentProvider provider = paymentProviderRegistry.resolve(request.getMetodoPagamento());

        Pagamento pagamento = new Pagamento();
        pagamento.setEscolaId(user.escolaId());
        pagamento.setAlunoId(request.getAlunoId());
        pagamento.setValor(request.getValor());
        pagamento.setMetodoPagamento(request.getMetodoPagamento());

        PaymentProviderResult providerResult = provider.createPayment(pagamento);
        pagamento.setProvider(providerResult.provider());
        pagamento.setTransacaoExterna(providerResult.transacaoExterna());
        pagamento.setStatus(providerResult.status());

        return mapper.toResponse(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponse confirmar(UUID publicId, CurrentUser user) {
        Pagamento pagamento = buscarPorPublicIdEEscola(publicId, user.escolaId());
        accessValidator.validateAccess(pagamento.getEscolaId(), user, pagamento.getAlunoId(), true);

        if (pagamento.getStatus() != PagamentoStatus.CONFIRMADO) {
            pagamento.setStatus(PagamentoStatus.CONFIRMADO);
        }

        return mapper.toResponse(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponse cancelar(UUID publicId, CurrentUser user) {
        Pagamento pagamento = buscarPorPublicIdEEscola(publicId, user.escolaId());
        accessValidator.validateAccess(pagamento.getEscolaId(), user, pagamento.getAlunoId(), true);

        if (pagamento.getStatus() != PagamentoStatus.CANCELADO) {
            pagamento.setStatus(PagamentoStatus.CANCELADO);
        }

        return mapper.toResponse(pagamentoRepository.save(pagamento));
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listar(CurrentUser user) {
        List<Pagamento> pagamentos;
        if (user.isAdmin()) {
            pagamentos = pagamentoRepository.findAllByEscolaId(user.escolaId());
        } else if (user.isResponsavel()) {
            List<Long> alunos = alunosClient.listarAlunosDoResponsavel(user.token());
            if (alunos.isEmpty()) {
                return Collections.emptyList();
            }
            pagamentos = pagamentoRepository.findAllByEscolaIdAndAlunoIdIn(user.escolaId(), alunos);
        } else {
            pagamentos = Collections.emptyList();
        }

        return mapper.toPagamentoResponses(pagamentos);
    }

    @Transactional
    public PagamentoResponse processarWebhook(String provider, WebhookRequest request) {
        Pagamento pagamento = pagamentoRepository.findByTransacaoExternaAndProviderAndEscolaId(
                        request.getTransacaoExterna(), provider.toUpperCase(Locale.ROOT), request.getEscolaId())
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));

        if (pagamento.getStatus() == request.getStatus()) {
            return mapper.toResponse(pagamento);
        }

        PaymentProvider paymentProvider = paymentProviderRegistry.resolveByName(provider);
        PagamentoStatus statusProvider = paymentProvider.handleWebhook(request.getPayload(), pagamento);
        pagamento.setStatus(statusProvider == null ? request.getStatus() : statusProvider);

        return mapper.toResponse(pagamentoRepository.save(pagamento));
    }

    private Pagamento buscarPorPublicIdEEscola(UUID publicId, String escolaId) {
        return pagamentoRepository.findByPublicIdAndEscolaId(publicId, escolaId)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
    }
}
