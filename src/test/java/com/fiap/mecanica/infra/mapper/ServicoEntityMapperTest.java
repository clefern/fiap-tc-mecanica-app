package com.fiap.mecanica.infra.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.infra.entity.ServicoEntity;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ServicoEntityMapperTest {

  private final ServicoEntityMapper mapper = Mappers.getMapper(ServicoEntityMapper.class);

  @Test
  @DisplayName("Deve converter ServicoEntity para Domain corretamente")
  void shouldConvertEntityToDomain() {
    ServicoEntity entity = new ServicoEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Troca de Óleo");
    entity.setDescricao("Troca de óleo completa");
    entity.setPrecoBase(new BigDecimal("150.00"));
    entity.setTempoEstimadoMinutos(45L);
    entity.setCategoria(CategoriaServico.MANUTENCAO_PREVENTIVA);
    entity.setAtivo(true);

    Servico domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(entity.getId(), domain.getId());
    assertEquals(entity.getNome(), domain.getNome());
    assertEquals(entity.getDescricao(), domain.getDescricao());
    assertEquals(entity.getPrecoBase(), domain.getPrecoBase());
    assertEquals(entity.getTempoEstimadoMinutos(), domain.getTempoEstimado().toMinutes());
    assertEquals(entity.getCategoria(), domain.getCategoria());
    assertEquals(entity.isAtivo(), domain.isAtivo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter ServicoEntity nulo para Domain")
  void shouldReturnNullWhenConvertingNullEntityToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve converter Domain para ServicoEntity corretamente")
  void shouldConvertDomainToEntity() {
    Servico domain =
        new Servico(
            UUID.randomUUID(),
            "Troca de Óleo",
            "Troca de óleo completa",
            new BigDecimal("150.00"),
            true,
            Duration.ofMinutes(45),
            CategoriaServico.MANUTENCAO_PREVENTIVA);

    ServicoEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(domain.getId(), entity.getId());
    assertEquals(domain.getNome(), entity.getNome());
    assertEquals(domain.getDescricao(), entity.getDescricao());
    assertEquals(domain.getPrecoBase(), entity.getPrecoBase());
    assertEquals(domain.getTempoEstimado().toMinutes(), entity.getTempoEstimadoMinutos());
    assertEquals(domain.getCategoria(), entity.getCategoria());
    assertEquals(domain.isAtivo(), entity.isAtivo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter Domain nulo para Entity")
  void shouldReturnNullWhenConvertingNullDomainToEntity() {
    assertNull(mapper.toEntity(null));
  }
}
