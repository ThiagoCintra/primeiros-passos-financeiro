package com.primeirospassos.financeiro.dto;

import com.primeirospassos.financeiro.enumtype.PagamentoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class WebhookRequest {
    @NotBlank
    private String escolaId;

    @NotBlank
    private String transacaoExterna;

    @NotNull
    private PagamentoStatus status;

    private Map<String, Object> payload = new HashMap<>();
}
