package com.primeirospassos.financeiro.dto;

import com.primeirospassos.financeiro.enumtype.PagamentoStatus;
import com.primeirospassos.financeiro.enumtype.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PagamentoResponse {
    private UUID publicId;
    private Long alunoId;
    private BigDecimal valor;
    private PaymentMethod metodoPagamento;
    private PagamentoStatus status;
    private String provider;
    private String transacaoExterna;
    private LocalDateTime dataPagamento;
}
