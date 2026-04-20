package com.primeirospassos.financeiro.service;

import com.primeirospassos.financeiro.dto.CreatePagamentoRequest;
import com.primeirospassos.financeiro.entity.Pagamento;
import com.primeirospassos.financeiro.enumtype.PagamentoStatus;
import com.primeirospassos.financeiro.enumtype.PaymentMethod;
import com.primeirospassos.financeiro.integration.AlunoDto;
import com.primeirospassos.financeiro.integration.AlunosClient;
import com.primeirospassos.financeiro.mapper.FinanceiroMapper;
import com.primeirospassos.financeiro.payment.PaymentProvider;
import com.primeirospassos.financeiro.payment.PaymentProviderRegistry;
import com.primeirospassos.financeiro.payment.PaymentProviderResult;
import com.primeirospassos.financeiro.repository.PagamentoRepository;
import com.primeirospassos.financeiro.security.CurrentUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PaymentProviderRegistry paymentProviderRegistry;

    @Spy
    private FinanceiroMapper mapper = Mappers.getMapper(FinanceiroMapper.class);

    @Mock
    private AccessValidator accessValidator;

    @Mock
    private AlunosClient alunosClient;

    @Mock
    private AntifraudeService antifraudeService;

    @InjectMocks
    private PagamentoService pagamentoService;

    @Test
    void shouldCreatePayment() {
        CurrentUser user = new CurrentUser(1L, "esc-1", "ADMIN", "Bearer token", "sess-1");
        CreatePagamentoRequest request = new CreatePagamentoRequest();
        request.setAlunoId(1L);
        request.setValor(BigDecimal.TEN);
        request.setMetodoPagamento(PaymentMethod.PIX);

        PaymentProvider provider = mock(PaymentProvider.class);
        when(paymentProviderRegistry.resolve(PaymentMethod.PIX)).thenReturn(provider);
        when(provider.createPayment(any(Pagamento.class))).thenReturn(new PaymentProviderResult("PIX", "TX-1", PagamentoStatus.PROCESSANDO));
        when(alunosClient.buscarAlunoPorId(1L, "Bearer token")).thenReturn(Optional.of(new AlunoDto()));
        when(antifraudeService.avaliar(request, user)).thenReturn(Optional.empty());
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("PIX", pagamentoService.criar(request, user).getProvider());
        verify(accessValidator).validateAdmin(user);
    }

    @Test
    void shouldFailWhenAlunoNotFound() {
        CurrentUser user = new CurrentUser(1L, "esc-1", "ADMIN", "Bearer token", "sess-1");
        CreatePagamentoRequest request = new CreatePagamentoRequest();
        request.setAlunoId(999L);
        request.setValor(BigDecimal.ONE);
        request.setMetodoPagamento(PaymentMethod.BOLETO);

        when(alunosClient.buscarAlunoPorId(999L, "Bearer token")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> pagamentoService.criar(request, user));
    }
}
