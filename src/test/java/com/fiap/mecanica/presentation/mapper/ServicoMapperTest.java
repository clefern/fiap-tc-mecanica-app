package com.fiap.mecanica.presentation.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.presentation.dto.ServicoRequest;
import com.fiap.mecanica.presentation.dto.ServicoResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServicoMapperTest {

  private final ServicoMapper mapper = new ServicoMapper();

  @Test
  @DisplayName("Deve converter ServicoRequest para Domain corretamente")
  void shouldConvertRequestToDomain() {
    ServicoRequest request =
        new ServicoRequest(
            "Troca de Óleo",
            "Troca de óleo completa",
            new BigDecimal("150.00"),
            45L,
            CategoriaServico.MANUTENCAO_PREVENTIVA,
            true);

    Servico domain = mapper.toDomain(request);

    assertNotNull(domain);
    assertEquals(request.getNome(), domain.getNome());
    assertEquals(request.getDescricao(), domain.getDescricao());
    assertEquals(request.getValorBase(), domain.getPrecoBase());
    assertEquals(request.getTempoEstimadoMinutos(), domain.getTempoEstimado().toMinutes());
    assertEquals(request.getCategoria(), domain.getCategoria());
  }

  @Test
  @DisplayName("Deve retornar null ao converter ServicoRequest nulo para Domain")
  void shouldReturnNullWhenConvertingNullRequestToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve converter Domain para ServicoResponse corretamente")
  void shouldConvertDomainToResponse() {
    Servico domain =
        new Servico(
            UUID.randomUUID(),
            "Troca de Óleo",
            "Troca de óleo completa",
            new BigDecimal("150.00"),
            true,
            Duration.ofMinutes(45),
            CategoriaServico.MANUTENCAO_PREVENTIVA);

    ServicoResponse response = mapper.toResponse(domain);

    assertNotNull(response);
    assertEquals(domain.getId(), response.getId());
    assertEquals(domain.getNome(), response.getNome());
    assertEquals(domain.getDescricao(), response.getDescricao());
    assertEquals(domain.getPrecoBase(), response.getValorBase());
    assertEquals(domain.getTempoEstimado().toMinutes(), response.getTempoEstimadoMinutos());
    assertEquals(domain.getCategoria(), response.getCategoria());
    assertEquals(domain.isAtivo(), response.isAtivo());
  }

  @Test
  @DisplayName("Deve retornar null ao converter Domain nulo para Response")
  void shouldReturnNullWhenConvertingNullDomainToResponse() {
    assertNull(mapper.toResponse(null));
  }
}
