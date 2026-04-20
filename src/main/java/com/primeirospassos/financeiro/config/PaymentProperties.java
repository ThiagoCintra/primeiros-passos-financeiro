package com.primeirospassos.financeiro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "payment.providers")
public class PaymentProperties {
    private String defaultProvider = "PIX";
}
