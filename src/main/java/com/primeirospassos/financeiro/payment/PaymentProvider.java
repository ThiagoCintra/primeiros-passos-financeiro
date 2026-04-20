package com.primeirospassos.financeiro.payment;

import com.primeirospassos.financeiro.entity.Pagamento;
import com.primeirospassos.financeiro.enumtype.PagamentoStatus;

import java.util.Map;

public interface PaymentProvider {
    String providerName();

    PaymentProviderResult createPayment(Pagamento pagamento);

    PagamentoStatus checkStatus(Pagamento pagamento);

    PagamentoStatus handleWebhook(Map<String, Object> payload, Pagamento pagamento);
}
