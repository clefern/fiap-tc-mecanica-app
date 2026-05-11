package com.fiap.mecanica.infra.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.infra.entity.PecaEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PecaEntityMapperTest {

  private final PecaEntityMapper mapper = Mappers.getMapper(PecaEntityMapper.class);

  @Test
  @DisplayName("Deve converter PecaEntity para Domain corretamente")
  void shouldConvertEntityToDomain() {
    PecaEntity entity = new PecaEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Pastilha");
    entity.setDescricao("Pastilha de freio");
    entity.setPrecoBase(new BigDecimal("250.00"));
    entity.setAtivo(true);
    entity.setFabricante("Bosch");
    entity.setCodigoFabricante("12345");
    entity.setModelo("Golf");
    entity.setQuantidadeEstoque(50);
    entity.setEstoqueMinimo(10);
    entity.setEstoqueMaximo(100);

    Peca domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(entity.getId(), domain.getId());
    assertEquals(entity.getNome(), domain.getNome());
    assertEquals(entity.getDescricao(), domain.getDescricao());
    assertEquals(entity.getPrecoBase(), domain.getPrecoBase());
    assertEquals(entity.isAtivo(), domain.isAtivo());
    assertEquals(entity.getFabricante(), domain.getFabricante());
    assertEquals(entity.getCodigoFabricante(), domain.getCodigoFabricante());
    assertEquals(entity.getModelo(), domain.getModelo());
    assertEquals(entity.getQuantidadeEstoque(), domain.getQuantidadeEstoque());
    assertEquals(entity.getEstoqueMinimo(), domain.getEstoqueMinimo());
    assertEquals(entity.getEstoqueMaximo(), domain.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter PecaEntity nulo para Domain")
  void shouldReturnNullWhenConvertingNullEntityToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve converter Domain para PecaEntity corretamente")
  void shouldConvertDomainToEntity() {
    Peca domain =
        new Peca(
            UUID.randomUUID(),
            "Pastilha",
            "Pastilha de freio",
            new BigDecimal("250.00"),
            true,
            "Bosch",
            "12345",
            "Golf",
            50,
            10,
            100);

    PecaEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(domain.getId(), entity.getId());
    assertEquals(domain.getNome(), entity.getNome());
    assertEquals(domain.getDescricao(), entity.getDescricao());
    assertEquals(domain.getPrecoBase(), entity.getPrecoBase());
    assertEquals(domain.isAtivo(), entity.isAtivo());
    assertEquals(domain.getFabricante(), entity.getFabricante());
    assertEquals(domain.getCodigoFabricante(), entity.getCodigoFabricante());
    assertEquals(domain.getModelo(), entity.getModelo());
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
