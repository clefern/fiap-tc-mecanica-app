package com.fiap.mecanica.infra.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.infra.entity.OrcamentoEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class OrcamentoEntityMapperTest {

  private final OrcamentoEntityMapper mapper = Mappers.getMapper(OrcamentoEntityMapper.class);

  @Test
  @DisplayName("Deve mapear Domain para Entity")
  void deveMapearDomainParaEntity() {
    Orcamento domain =
        Orcamento.builder()
            .id(UUID.randomUUID())
            .codigo("ORC-001")
            .valorTotal(BigDecimal.TEN)
            .status(StatusOrcamento.GERADO)
            .build();

    OrcamentoEntity entity = mapper.toEntity(domain);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(domain.getId());
    assertThat(entity.getCodigo()).isEqualTo(domain.getCodigo());
    assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
  }

  @Test
  @DisplayName("Deve mapear Entity para Domain")
  void deveMapearEntityParaDomain() {
    OrcamentoEntity entity =
        OrcamentoEntity.builder()
            .id(UUID.randomUUID())
            .codigo("ORC-001")
            .valorTotal(BigDecimal.TEN)
            .status(StatusOrcamento.GERADO)
            .build();

    Orcamento domain = mapper.toDomain(entity);

    assertThat(domain).isNotNull();
    assertThat(domain.getId()).isEqualTo(entity.getId());
    assertThat(domain.getCodigo()).isEqualTo(entity.getCodigo());
    assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
  }
}
