package com.primeirospassos.financeiro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient alunosWebClient(@Value("${alunos.service.url}") String alunosServiceUrl) {
        return WebClient.builder()
                .baseUrl(alunosServiceUrl)
                .build();
    }

    @Bean
    public WebClient authWebClient(@Value("${login.service.url}") String loginServiceUrl) {
        return WebClient.builder()
                .baseUrl(loginServiceUrl)
                .build();
    }
}
