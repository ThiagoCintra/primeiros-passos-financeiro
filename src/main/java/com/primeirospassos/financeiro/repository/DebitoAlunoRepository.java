package com.primeirospassos.financeiro.repository;

import com.primeirospassos.financeiro.entity.DebitoAluno;
import com.primeirospassos.financeiro.enumtype.DebitoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DebitoAlunoRepository extends JpaRepository<DebitoAluno, Long> {

    Optional<DebitoAluno> findByPublicIdAndEscolaId(UUID publicId, String escolaId);

    List<DebitoAluno> findAllByEscolaId(String escolaId);

    List<DebitoAluno> findAllByEscolaIdAndAlunoIdIn(String escolaId, List<Long> alunoIds);

    @Query("select coalesce(sum(d.valor), 0) from DebitoAluno d where d.escolaId = :escolaId and d.status = :status")
    BigDecimal sumByEscolaAndStatus(@Param("escolaId") String escolaId, @Param("status") DebitoStatus status);

    @Query("select coalesce(sum(d.valor), 0) from DebitoAluno d where d.escolaId = :escolaId and d.status = :status and d.alunoId in :alunoIds")
    BigDecimal sumByEscolaAndStatusAndAlunoIdIn(@Param("escolaId") String escolaId, @Param("status") DebitoStatus status, @Param("alunoIds") List<Long> alunoIds);
}
