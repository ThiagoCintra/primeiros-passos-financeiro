package com.primeirospassos.financeiro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "antifraude_registros", schema = "financeiro")
public class AntifraudeRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pagamento_public_id", nullable = false)
    private UUID pagamentoPublicId;

    @Column(name = "escola_id", nullable = false)
    private String escolaId;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(nullable = false)
    private String motivo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void onCreate() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}
