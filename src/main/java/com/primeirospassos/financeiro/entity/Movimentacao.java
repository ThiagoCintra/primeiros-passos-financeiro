package com.primeirospassos.financeiro.entity;

import com.primeirospassos.financeiro.enumtype.MovimentacaoTipo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "movimentacoes", schema = "financeiro")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "escola_id", nullable = false)
    private String escolaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovimentacaoTipo tipo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "aluno_id")
    private Long alunoId;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDateTime dataMovimentacao;

    @PrePersist
    public void onCreate() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
        if (dataMovimentacao == null) {
            dataMovimentacao = LocalDateTime.now();
        }
    }
}
