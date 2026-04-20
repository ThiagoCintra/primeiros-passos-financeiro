package com.primeirospassos.financeiro.mapper;

import com.primeirospassos.financeiro.dto.DebitoResponse;
import com.primeirospassos.financeiro.dto.PagamentoResponse;
import com.primeirospassos.financeiro.entity.DebitoAluno;
import com.primeirospassos.financeiro.entity.Pagamento;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FinanceiroMapper {
    PagamentoResponse toResponse(Pagamento pagamento);

    List<PagamentoResponse> toPagamentoResponses(List<Pagamento> pagamentos);

    DebitoResponse toResponse(DebitoAluno debitoAluno);

    List<DebitoResponse> toDebitoResponses(List<DebitoAluno> debitos);
}
