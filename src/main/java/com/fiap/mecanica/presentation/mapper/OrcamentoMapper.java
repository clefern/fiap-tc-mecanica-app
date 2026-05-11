package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.presentation.dto.OrcamentoResponse;
import org.springframework.stereotype.Component;

@Component
public class OrcamentoMapper {

  public OrcamentoResponse toResponse(Orcamento domain) {
    if (domain == null) {
      return null;
    }

    return OrcamentoResponse.builder()
        .id(domain.getId())
        .codigo(domain.getCodigo())
        .ordemServicoId(domain.getOrdemServicoId())
        .mecanicoDiagnosticoId(domain.getMecanicoDiagnosticoId())
        .dataEmissao(domain.getDataEmissao())
        .dataValidade(domain.getDataValidade())
        .valorTotalMateriais(domain.getValorTotalMateriais())
        .valorTotalMaoDeObra(domain.getValorTotalMaoDeObra())
        .valorImpostos(domain.getValorImpostos())
        .valorTotal(domain.getValorTotal())
        .status(domain.getStatus())
        .urlPdf(domain.getUrlPdf())
        .build();
  }
}
