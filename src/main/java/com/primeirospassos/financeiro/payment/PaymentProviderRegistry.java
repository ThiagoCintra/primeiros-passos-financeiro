package com.primeirospassos.financeiro.payment;

import com.primeirospassos.financeiro.config.PaymentProperties;
import com.primeirospassos.financeiro.enumtype.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentProviderRegistry {

    private final List<PaymentProvider> providers;
    private final PaymentProperties paymentProperties;

    public PaymentProvider resolve(PaymentMethod method) {
        String providerName = switch (method) {
            case PIX -> "PIX";
            case BOLETO -> "BOLETO";
            case DEBITO_AUTOMATICO -> "DEBITO_AUTOMATICO";
            default -> paymentProperties.getDefaultProvider();
        };
        return resolveByName(providerName);
    }

    public PaymentProvider resolveByName(String providerName) {
        Map<String, PaymentProvider> providerMap = providers.stream()
                .collect(Collectors.toMap(p -> p.providerName().toUpperCase(Locale.ROOT), Function.identity()));

        PaymentProvider provider = providerMap.get(providerName.toUpperCase(Locale.ROOT));
        if (provider == null) {
            throw new IllegalArgumentException("Provider não suportado: " + providerName);
        }
        return provider;
    }
}
