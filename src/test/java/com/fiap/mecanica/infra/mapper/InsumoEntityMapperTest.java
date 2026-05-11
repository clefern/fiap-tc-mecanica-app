package com.fiap.mecanica.infra.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.infra.entity.InsumoEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class InsumoEntityMapperTest {

  private final InsumoEntityMapper mapper = Mappers.getMapper(InsumoEntityMapper.class);

  @Test
  @DisplayName("Deve converter InsumoEntity para Domain corretamente")
  void shouldConvertEntityToDomain() {
    InsumoEntity entity = new InsumoEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Óleo");
    entity.setDescricao("Óleo 5w30");
    entity.setPrecoBase(new BigDecimal("50.00"));
    entity.setAtivo(true);
    entity.setUnidadeMedida("LITRO");
    entity.setQuantidadeEstoque(100);
    entity.setEstoqueMinimo(20);
    entity.setEstoqueMaximo(200);

    Insumo domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(entity.getId(), domain.getId());
    assertEquals(entity.getNome(), domain.getNome());
    assertEquals(entity.getDescricao(), domain.getDescricao());
    assertEquals(entity.getPrecoBase(), domain.getPrecoBase());
    assertEquals(entity.isAtivo(), domain.isAtivo());
    assertEquals(entity.getUnidadeMedida(), domain.getUnidadeMedida());
    assertEquals(entity.getQuantidadeEstoque(), domain.getQuantidadeEstoque());
    assertEquals(entity.getEstoqueMinimo(), domain.getEstoqueMinimo());
    assertEquals(entity.getEstoqueMaximo(), domain.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter InsumoEntity nulo para Domain")
  void shouldReturnNullWhenConvertingNullEntityToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve converter Domain para InsumoEntity corretamente")
  void shouldConvertDomainToEntity() {
    Insumo domain =
        new Insumo(
            UUID.randomUUID(),
            "Óleo",
            "Óleo 5w30",
            new BigDecimal("50.00"),
            true,
            "LITRO",
            100,
            20,
            200);

    InsumoEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(domain.getId(), entity.getId());
    assertEquals(domain.getNome(), entity.getNome());
    assertEquals(domain.getDescricao(), entity.getDescricao());
    assertEquals(domain.getPrecoBase(), entity.getPrecoBase());
    assertEquals(domain.isAtivo(), entity.isAtivo());
    assertEquals(domain.getUnidadeMedida(), entity.getUnidadeMedida());
    assertEquals(domain.getQuantidadeEstoque(), entity.getQuantidadeEstoque());
    assertEquals(domain.getEstoqueMinimo(), entity.getEstoqueMinimo());
    assertEquals(domain.getEstoqueMaximo(), entity.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter Domain nulo para Entity")
  void shouldReturnNullWhenConvertingNullDomainToEntity() {
    assertNull(mapper.toEntity(null));
  }
}
