package com.primeirospassos.financeiro.service;

import com.primeirospassos.financeiro.dto.CreatePagamentoRequest;
import com.primeirospassos.financeiro.entity.Pagamento;
import com.primeirospassos.financeiro.enumtype.PaymentMethod;
import com.primeirospassos.financeiro.repository.AntifraudeRegistroRepository;
import com.primeirospassos.financeiro.repository.PagamentoRepository;
import com.primeirospassos.financeiro.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AntifraudeServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private AntifraudeRegistroRepository antifraudeRegistroRepository;

    @InjectMocks
    private AntifraudeService antifraudeService;

    private CurrentUser admin;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(antifraudeService, "valorAltoLimite", new BigDecimal("5000"));
        ReflectionTestUtils.setField(antifraudeService, "maxTentativas", 3L);
        ReflectionTestUtils.setField(antifraudeService, "janelaTentativasMinutos", 10L);
        ReflectionTestUtils.setField(antifraudeService, "janelaDuplicidadeMinutos", 5L);
        admin = new CurrentUser(1L, "esc-1", "ADMIN", "Bearer token", "sess-1");
    }

    @Test
    void shouldFlagHighValue() {
        CreatePagamentoRequest request = request(new BigDecimal("5000.00"));

        Optional<String> result = antifraudeService.avaliar(request, admin);

        assertTrue(result.isPresent());
        assertEquals("valor alto", result.get());
    }

    @Test
    void shouldFlagMultipleAttempts() {
        CreatePagamentoRequest request = request(new BigDecimal("100.00"));
        when(pagamentoRepository.countByEscolaIdAndAlunoIdAndDataPagamentoAfter(eq("esc-1"), eq(10L), any())).thenReturn(3L);

        Optional<String> result = antifraudeService.avaliar(request, admin);

        assertTrue(result.isPresent());
        assertEquals("múltiplas tentativas", result.get());
    }

    @Test
    void shouldFlagDuplicate() {
        CreatePagamentoRequest request = request(new BigDecimal("100.00"));
        when(pagamentoRepository.countByEscolaIdAndAlunoIdAndDataPagamentoAfter(eq("esc-1"), eq(10L), any())).thenReturn(0L);
        when(pagamentoRepository.existsByEscolaIdAndAlunoIdAndValorAndMetodoPagamentoAndDataPagamentoAfter(
                eq("esc-1"), eq(10L), eq(new BigDecimal("100.00")), eq(PaymentMethod.PIX), any())).thenReturn(true);

        Optional<String> result = antifraudeService.avaliar(request, admin);

        assertTrue(result.isPresent());
        assertEquals("duplicidade", result.get());
    }

    @Test
    void shouldPersistFraudRecord() {
        Pagamento pagamento = new Pagamento();
        pagamento.setPublicId(UUID.randomUUID());
        pagamento.setEscolaId("esc-1");
        pagamento.setAlunoId(10L);

        antifraudeService.registrarSuspeita(pagamento, "valor alto");

        ArgumentCaptor<com.primeirospassos.financeiro.entity.AntifraudeRegistro> captor =
                ArgumentCaptor.forClass(com.primeirospassos.financeiro.entity.AntifraudeRegistro.class);
        verify(antifraudeRegistroRepository).save(captor.capture());
        assertEquals("esc-1", captor.getValue().getEscolaId());
        assertEquals(10L, captor.getValue().getAlunoId());
        assertEquals("valor alto", captor.getValue().getMotivo());
        assertNotNull(captor.getValue().getPagamentoPublicId());
    }

    private CreatePagamentoRequest request(BigDecimal valor) {
        CreatePagamentoRequest request = new CreatePagamentoRequest();
        request.setAlunoId(10L);
        request.setValor(valor);
        request.setMetodoPagamento(PaymentMethod.PIX);
        return request;
    }
}
