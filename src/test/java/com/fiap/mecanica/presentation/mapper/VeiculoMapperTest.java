package com.fiap.mecanica.presentation.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.presentation.dto.VeiculoRequest;
import com.fiap.mecanica.presentation.dto.VeiculoResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VeiculoMapperTest {

  private final VeiculoMapper mapper = new VeiculoMapper();

  @Test
  @DisplayName("Deve converter VeiculoRequest para Veiculo corretamente")
  void shouldConvertRequestToDomain() {
    VeiculoRequest request = new VeiculoRequest("ABC1234", "Toyota", "Corolla", 2020);

    Veiculo veiculo = mapper.toDomain(request);

    assertNotNull(veiculo);
    assertEquals(request.getPlaca(), veiculo.getPlaca().value());
    assertEquals(request.getMarca(), veiculo.getMarca());
    assertEquals(request.getModelo(), veiculo.getModelo());
    assertEquals(request.getAno(), veiculo.getAno());
  }

  @Test
  @DisplayName("Deve converter Veiculo para VeiculoResponse corretamente")
  void shouldConvertDomainToResponse() {
    Veiculo veiculo =
        new Veiculo(
            com.fiap.mecanica.domain.valueobject.PlacaVeiculo.of("ABC1234"),
            "Toyota",
            "Corolla",
            2020);
    UUID id = UUID.randomUUID();
    veiculo.setId(id);

    VeiculoResponse response = mapper.toResponse(veiculo);

    assertNotNull(response);
    assertEquals(id, response.getId());
    assertEquals(veiculo.getPlaca().value(), response.getPlaca());
    assertEquals(veiculo.getMarca(), response.getMarca());
    assertEquals(veiculo.getModelo(), response.getModelo());
    assertEquals(veiculo.getAno(), response.getAno());
  }

  @Test
  @DisplayName("Deve retornar null ao converter VeiculoRequest nulo para Domain")
  void shouldReturnNullWhenConvertingNullRequestToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve retornar null ao converter Veiculo nulo para Response")
  void shouldReturnNullWhenConvertingNullDomainToResponse() {
    assertNull(mapper.toResponse(null));
  }
}
