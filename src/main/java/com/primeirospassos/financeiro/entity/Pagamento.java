package com.primeirospassos.financeiro.entity;

import com.primeirospassos.financeiro.enumtype.PagamentoStatus;
import com.primeirospassos.financeiro.enumtype.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pagamentos", schema = "financeiro")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "escola_id", nullable = false)
    private String escolaId;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false)
    private PaymentMethod metodoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PagamentoStatus status;

    @Column(nullable = false)
    private String provider;

    @Column(name = "transacao_externa")
    private String transacaoExterna;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDateTime dataPagamento;

    @PrePersist
    public void onCreate() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
        if (dataPagamento == null) {
            dataPagamento = LocalDateTime.now();
        }
        if (status == null) {
            status = PagamentoStatus.PENDENTE;
        }
    }
}
