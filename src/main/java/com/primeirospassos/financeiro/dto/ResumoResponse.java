package com.primeirospassos.financeiro.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ResumoResponse {
    BigDecimal totalEntradas;
    BigDecimal totalSaidas;
    BigDecimal debitosPendentes;
    BigDecimal pagamentosConfirmados;
}
