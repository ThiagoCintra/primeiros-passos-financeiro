package com.primeirospassos.financeiro.dto;

import com.primeirospassos.financeiro.enumtype.DebitoStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DebitoResponse {
    private UUID publicId;
    private Long alunoId;
    private String descricao;
    private BigDecimal valor;
    private DebitoStatus status;
    private LocalDateTime dataCriacao;
}
