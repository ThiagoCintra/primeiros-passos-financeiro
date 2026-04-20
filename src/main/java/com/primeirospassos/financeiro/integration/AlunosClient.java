package com.primeirospassos.financeiro.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlunosClient {

    private final WebClient alunosWebClient;

    public Optional<AlunoDto> buscarAlunoPorId(Long alunoId, String authorization) {
        return alunosWebClient.get()
                .uri("/api/v1/alunos/{id}", alunoId)
                .header("Authorization", authorization)
                .retrieve()
                .bodyToMono(AlunoDto.class)
                .doOnError(error -> log.warn("Falha ao buscar aluno {}", alunoId, error))
                .onErrorResume(error -> reactor.core.publisher.Mono.empty())
                .blockOptional();
    }

    public List<Long> listarAlunosDoResponsavel(String authorization) {
        List<AlunoDto> alunos = alunosWebClient.get()
                .uri("/api/v1/alunos/responsavel")
                .header("Authorization", authorization)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AlunoDto>>() {
                })
                .doOnError(error -> log.warn("Falha ao buscar alunos do responsável", error))
                .onErrorReturn(Collections.emptyList())
                .block();

        if (alunos == null) {
            return Collections.emptyList();
        }
        return alunos.stream().map(AlunoDto::getId).toList();
    }

    public boolean alunoPertenceAoResponsavel(Long alunoId, String authorization) {
        return listarAlunosDoResponsavel(authorization).contains(alunoId);
    }
}
