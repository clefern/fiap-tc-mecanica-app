package com.fiap.mecanica.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VeiculoNaoEncontradoExceptionTest {

  @Test
  @DisplayName("Deve construir mensagem e código a partir do ID")
  void shouldBuildMessageAndCodeFromId() {
    UUID id = UUID.randomUUID();

    VeiculoNaoEncontradoException ex = new VeiculoNaoEncontradoException(id);

    assertEquals("VEICULO_404", ex.getCode());
    assertEquals("Veículo não encontrado com ID: %s".formatted(id), ex.getMessage());
  }

  @Test
  @DisplayName("Deve aceitar mensagem customizada mantendo o código")
  void shouldAcceptCustomMessage() {
    String message = "Veículo não encontrado";

    VeiculoNaoEncontradoException ex = new VeiculoNaoEncontradoException(message);

    assertEquals("VEICULO_404", ex.getCode());
    assertEquals(message, ex.getMessage());
  }
}
