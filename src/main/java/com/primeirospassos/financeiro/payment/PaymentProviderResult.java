package com.primeirospassos.financeiro.payment;

import com.primeirospassos.financeiro.enumtype.PagamentoStatus;

public record PaymentProviderResult(String provider, String transacaoExterna, PagamentoStatus status) {
}
