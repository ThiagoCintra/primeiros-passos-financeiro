package com.primeirospassos.financeiro.dto;

import com.primeirospassos.financeiro.enumtype.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePagamentoRequest {

    @NotNull
    private Long alunoId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal valor;

    @NotNull
    private PaymentMethod metodoPagamento;
}
