package com.fiap.mecanica.presentation.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.presentation.dto.InsumoRequest;
import com.fiap.mecanica.presentation.dto.InsumoResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InsumoMapperTest {

  private final InsumoMapper mapper = new InsumoMapper();

  @Test
  @DisplayName("Deve converter InsumoRequest para Domain corretamente")
  void shouldConvertRequestToDomain() {
    InsumoRequest request =
        new InsumoRequest(
            "Óleo", "Óleo 5w30", new BigDecimal("50.00"), "LITRO", true, 100, 20, 200);

    Insumo domain = mapper.toDomain(request);

    assertNotNull(domain);
    assertEquals(request.getNome(), domain.getNome());
    assertEquals(request.getDescricao(), domain.getDescricao());
    assertEquals(request.getPrecoBase(), domain.getPrecoBase());
    assertEquals(request.isAtivo(), domain.isAtivo());
    assertEquals(request.getUnidadeMedida(), domain.getUnidadeMedida());
    assertEquals(request.getQuantidadeEstoque(), domain.getQuantidadeEstoque());
    assertEquals(request.getEstoqueMinimo(), domain.getEstoqueMinimo());
    assertEquals(request.getEstoqueMaximo(), domain.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter InsumoRequest nulo para Domain")
  void shouldReturnNullWhenConvertingNullRequestToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve converter Domain para InsumoResponse corretamente")
  void shouldConvertDomainToResponse() {
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

    InsumoResponse response = mapper.toResponse(domain);

    assertNotNull(response);
    assertEquals(domain.getId(), response.getId());
    assertEquals(domain.getNome(), response.getNome());
    assertEquals(domain.getDescricao(), response.getDescricao());
    assertEquals(domain.getPrecoBase(), response.getPrecoBase());
    assertEquals(domain.getUnidadeMedida(), response.getUnidadeMedida());
    assertEquals(domain.isAtivo(), response.isAtivo());
    assertEquals(domain.getQuantidadeEstoque(), response.getQuantidadeEstoque());
    assertEquals(domain.getEstoqueMinimo(), response.getEstoqueMinimo());
    assertEquals(domain.getEstoqueMaximo(), response.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter Domain nulo para Response")
  void shouldReturnNullWhenConvertingNullDomainToResponse() {
    assertNull(mapper.toResponse(null));
  }
}
