package com.fiap.mecanica.presentation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.presentation.dto.OrcamentoResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrcamentoMapperTest {

  private final OrcamentoMapper mapper = new OrcamentoMapper();

  @Test
  @DisplayName("Deve mapear Domain para Response")
  void deveMapearDomainParaResponse() {
    Orcamento orcamento =
        Orcamento.builder()
            .id(UUID.randomUUID())
            .codigo("ORC-001")
            .valorTotal(BigDecimal.TEN)
            .status(StatusOrcamento.GERADO)
            .build();

    OrcamentoResponse response = mapper.toResponse(orcamento);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(orcamento.getId());
    assertThat(response.getCodigo()).isEqualTo(orcamento.getCodigo());
    assertThat(response.getValorTotal()).isEqualTo(orcamento.getValorTotal());
    assertThat(response.getStatus()).isEqualTo(orcamento.getStatus());
  }

  @Test
  @DisplayName("Deve retornar null quando Domain for null")
  void deveRetornarNullQuandoDomainForNull() {
    OrcamentoResponse response = mapper.toResponse(null);

    assertThat(response).isNull();
  }
}
