package com.primeirospassos.financeiro.repository;

import com.primeirospassos.financeiro.entity.Movimentacao;
import com.primeirospassos.financeiro.enumtype.MovimentacaoTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    Optional<Movimentacao> findByPublicIdAndEscolaId(UUID publicId, String escolaId);

    @Query("select coalesce(sum(m.valor), 0) from Movimentacao m where m.escolaId = :escolaId and m.tipo = :tipo")
    BigDecimal sumByEscolaAndTipo(@Param("escolaId") String escolaId, @Param("tipo") MovimentacaoTipo tipo);

    @Query("select coalesce(sum(m.valor), 0) from Movimentacao m where m.escolaId = :escolaId and m.tipo = :tipo and m.alunoId in :alunoIds")
    BigDecimal sumByEscolaAndTipoAndAlunoIdIn(@Param("escolaId") String escolaId, @Param("tipo") MovimentacaoTipo tipo, @Param("alunoIds") List<Long> alunoIds);
}
