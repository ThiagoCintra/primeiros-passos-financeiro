package com.primeirospassos.financeiro.payment;

import com.primeirospassos.financeiro.entity.Pagamento;
import com.primeirospassos.financeiro.enumtype.PagamentoStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class AutomaticDebitProvider implements PaymentProvider {

    @Override
    public String providerName() {
        return "DEBITO_AUTOMATICO";
    }

    @Override
    public PaymentProviderResult createPayment(Pagamento pagamento) {
        return new PaymentProviderResult(providerName(), "DDA-" + UUID.randomUUID(), PagamentoStatus.PROCESSANDO);
    }

    @Override
    public PagamentoStatus checkStatus(Pagamento pagamento) {
        return pagamento.getStatus();
    }

    @Override
    public PagamentoStatus handleWebhook(Map<String, Object> payload, Pagamento pagamento) {
        Object status = payload.get("status");
        if (status == null) {
            return pagamento.getStatus();
        }
        try {
            return PagamentoStatus.valueOf(status.toString().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return pagamento.getStatus();
        }
    }
}
