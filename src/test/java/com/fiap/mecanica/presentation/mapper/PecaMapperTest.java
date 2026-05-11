package com.fiap.mecanica.presentation.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.presentation.dto.PecaRequest;
import com.fiap.mecanica.presentation.dto.PecaResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PecaMapperTest {

  private final PecaMapper mapper = new PecaMapper();

  @Test
  @DisplayName("Deve converter PecaRequest para Domain corretamente")
  void shouldConvertRequestToDomain() {
    PecaRequest request =
        new PecaRequest(
            "Pastilha",
            "Pastilha de freio",
            new BigDecimal("250.00"),
            "Bosch",
            "12345",
            "Golf",
            true,
            50,
            10,
            100);

    Peca domain = mapper.toDomain(request);

    assertNotNull(domain);
    assertEquals(request.getNome(), domain.getNome());
    assertEquals(request.getDescricao(), domain.getDescricao());
    assertEquals(request.getPrecoBase(), domain.getPrecoBase());
    assertEquals(request.getFabricante(), domain.getFabricante());
    assertEquals(request.getCodigoFabricante(), domain.getCodigoFabricante());
    assertEquals(request.getModelo(), domain.getModelo());
    assertEquals(request.isAtivo(), domain.isAtivo());
    assertEquals(request.getQuantidadeEstoque(), domain.getQuantidadeEstoque());
    assertEquals(request.getEstoqueMinimo(), domain.getEstoqueMinimo());
    assertEquals(request.getEstoqueMaximo(), domain.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter PecaRequest nulo para Domain")
  void shouldReturnNullWhenConvertingNullRequestToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve converter Domain para PecaResponse corretamente")
  void shouldConvertDomainToResponse() {
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

    PecaResponse response = mapper.toResponse(domain);

    assertNotNull(response);
    assertEquals(domain.getId(), response.getId());
    assertEquals(domain.getNome(), response.getNome());
    assertEquals(domain.getDescricao(), response.getDescricao());
    assertEquals(domain.getPrecoBase(), response.getPrecoBase());
    assertEquals(domain.getFabricante(), response.getFabricante());
    assertEquals(domain.getCodigoFabricante(), response.getCodigoFabricante());
    assertEquals(domain.getModelo(), response.getModelo());
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
