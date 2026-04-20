package com.primeirospassos.financeiro.repository;

import com.primeirospassos.financeiro.entity.Pagamento;
import com.primeirospassos.financeiro.enumtype.PagamentoStatus;
import com.primeirospassos.financeiro.enumtype.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByPublicIdAndEscolaId(UUID publicId, String escolaId);

    List<Pagamento> findAllByEscolaId(String escolaId);

    List<Pagamento> findAllByEscolaIdAndAlunoIdIn(String escolaId, List<Long> alunoIds);

    Optional<Pagamento> findByTransacaoExternaAndProviderAndEscolaId(String transacaoExterna, String provider, String escolaId);

    long countByEscolaIdAndAlunoIdAndDataPagamentoAfter(String escolaId, Long alunoId, LocalDateTime dataPagamento);

    boolean existsByEscolaIdAndAlunoIdAndValorAndMetodoPagamentoAndDataPagamentoAfter(
            String escolaId,
            Long alunoId,
            BigDecimal valor,
            PaymentMethod metodoPagamento,
            LocalDateTime dataPagamento);

    @Query("select coalesce(sum(p.valor), 0) from Pagamento p where p.escolaId = :escolaId and p.status = :status")
    BigDecimal sumByEscolaAndStatus(@Param("escolaId") String escolaId, @Param("status") PagamentoStatus status);

    @Query("select coalesce(sum(p.valor), 0) from Pagamento p where p.escolaId = :escolaId and p.status = :status and p.alunoId in :alunoIds")
    BigDecimal sumByEscolaAndStatusAndAlunoIdIn(@Param("escolaId") String escolaId, @Param("status") PagamentoStatus status, @Param("alunoIds") List<Long> alunoIds);
}
