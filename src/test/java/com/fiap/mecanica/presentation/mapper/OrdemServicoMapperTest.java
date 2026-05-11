package com.fiap.mecanica.presentation.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.presentation.dto.AdicionarItemRequest;
import com.fiap.mecanica.presentation.dto.OrdemServicoResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrdemServicoMapperTest {

  private final OrdemServicoMapper mapper = new OrdemServicoMapper();

  @Test
  @DisplayName("Deve converter Domain para Response corretamente")
  void shouldConvertDomainToResponse() {
    // Arrange
    UUID id = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    OrdemServico domain =
        OrdemServico.builder()
            .id(id)
            .clienteId(clienteId)
            .veiculoId(veiculoId)
            .codigo("OS-2024-001")
            .status(StatusOS.RECEBIDA)
            .valorTotal(new BigDecimal("150.00"))
            .dataEntrada(now)
            .dataPrevisao(now.plusDays(2))
            .dataFechamento(null)
            .observacoes("Teste de observação")
            .build();

    // Act
    OrdemServicoResponse response = mapper.toResponse(domain);

    // Assert
    assertNotNull(response);
    assertEquals(domain.getId(), response.getId());
    assertEquals(domain.getClienteId(), response.getClienteId());
    assertEquals(domain.getVeiculoId(), response.getVeiculoId());
    assertEquals(domain.getCodigo(), response.getCodigo());
    assertEquals(domain.getStatus(), response.getStatus());
    assertEquals(domain.getValorTotal(), response.getValorTotal());
    assertEquals(domain.getDataEntrada(), response.getDataEntrada());
    assertEquals(domain.getDataPrevisao(), response.getDataPrevisao());
    assertEquals(domain.getDataFechamento(), response.getDataFechamento());
    assertEquals(domain.getObservacoes(), response.getObservacoes());
  }

  @Test
  @DisplayName("Deve retornar null ao converter Domain nulo para Response")
  void shouldReturnNullWhenConvertingNullDomainToResponse() {
    assertNull(mapper.toResponse(null));
  }

  @Test
  @DisplayName("Deve converter ItemRequest para Domain corretamente")
  void shouldConvertItemRequestToDomain() {
    AdicionarItemRequest request = new AdicionarItemRequest();
    request.setTipo(TipoItem.SERVICO);
    request.setDescricao("Troca de óleo");
    request.setValorUnitario(new BigDecimal("100.00"));
    request.setQuantidade(1);
    request.setReferenciaId(UUID.randomUUID());

    ItemOrdemServico item = mapper.toDomain(request);

    assertNotNull(item);
    assertNotNull(item.getId());
    assertEquals(request.getTipo(), item.getTipo());
    assertEquals(request.getDescricao(), item.getDescricao());
    assertEquals(request.getValorUnitario(), item.getValorUnitario());
    assertEquals(request.getQuantidade(), item.getQuantidade());
    assertEquals(request.getReferenciaId(), item.getReferenciaId());
  }

  @Test
  @DisplayName("Deve converter Domain com itens para Response corretamente")
  void shouldConvertDomainWithItemsToResponse() {
    // Arrange
    UUID id = UUID.randomUUID();
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.PECA)
            .descricao("Peça Teste")
            .valorUnitario(new BigDecimal("50.00"))
            .quantidade(2)
            .build();

    OrdemServico domain =
        OrdemServico.builder()
            .id(id)
            .clienteId(UUID.randomUUID())
            .veiculoId(UUID.randomUUID())
            .codigo("OS-WITH-ITEMS")
            .status(StatusOS.RECEBIDA)
            .valorTotal(new BigDecimal("100.00"))
            .itens(Collections.singletonList(item))
            .build();

    // Act
    OrdemServicoResponse response = mapper.toResponse(domain);

    // Assert
    assertNotNull(response);
    assertNotNull(response.getItens());
    assertEquals(1, response.getItens().size());
    assertEquals(item.getDescricao(), response.getItens().get(0).getDescricao());
    assertEquals(item.getSubtotal(), response.getItens().get(0).getSubtotal());
  }

  @Test
  @DisplayName("Deve retornar null ao converter ItemRequest nulo para Domain")
  void shouldReturnNullWhenConvertingNullItemRequestToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve retornar lista vazia se itens forem nulos no Domain")
  void shouldReturnEmptyListIfItemsAreNullInDomain() {
    // Arrange
    OrdemServico domain =
        OrdemServico.builder()
            .id(UUID.randomUUID())
            .itens(null) // Explicitly set to null to trigger branch
            .build();

    // Act
    OrdemServicoResponse response = mapper.toResponse(domain);

    // Assert
    assertNotNull(response);
    assertNotNull(response.getItens());
    assertEquals(0, response.getItens().size());
  }
}
