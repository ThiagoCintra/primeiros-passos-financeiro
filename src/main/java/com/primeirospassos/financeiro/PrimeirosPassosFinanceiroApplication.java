package com.primeirospassos.financeiro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PrimeirosPassosFinanceiroApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrimeirosPassosFinanceiroApplication.class, args);
    }
}
