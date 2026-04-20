package com.primeirospassos.financeiro.controller;

import com.primeirospassos.financeiro.dto.CreatePagamentoRequest;
import com.primeirospassos.financeiro.dto.DebitoResponse;
import com.primeirospassos.financeiro.dto.PagamentoResponse;
import com.primeirospassos.financeiro.dto.ResumoResponse;
import com.primeirospassos.financeiro.dto.WebhookRequest;
import com.primeirospassos.financeiro.security.AuthenticatedUserService;
import com.primeirospassos.financeiro.service.DebitoService;
import com.primeirospassos.financeiro.service.PagamentoService;
import com.primeirospassos.financeiro.service.ResumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final PagamentoService pagamentoService;
    private final DebitoService debitoService;
    private final ResumoService resumoService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoResponse criarPagamento(@Valid @RequestBody CreatePagamentoRequest request) {
        return pagamentoService.criar(request, authenticatedUserService.currentUser());
    }

    @PostMapping("/pagamentos/{publicId}/confirmar")
    public PagamentoResponse confirmarPagamento(@PathVariable UUID publicId) {
        return pagamentoService.confirmar(publicId, authenticatedUserService.currentUser());
    }

    @PostMapping("/pagamentos/{publicId}/cancelar")
    public PagamentoResponse cancelarPagamento(@PathVariable UUID publicId) {
        return pagamentoService.cancelar(publicId, authenticatedUserService.currentUser());
    }

    @GetMapping("/pagamentos")
    public List<PagamentoResponse> listarPagamentos() {
        return pagamentoService.listar(authenticatedUserService.currentUser());
    }

    @GetMapping("/debitos")
    public List<DebitoResponse> listarDebitos() {
        return debitoService.listar(authenticatedUserService.currentUser());
    }

    @GetMapping("/resumo")
    public ResumoResponse resumo() {
        return resumoService.gerar(authenticatedUserService.currentUser());
    }

    @PostMapping("/webhook/{provider}")
    public PagamentoResponse webhook(@PathVariable String provider, @Valid @RequestBody WebhookRequest request) {
        return pagamentoService.processarWebhook(provider, request);
    }
}
