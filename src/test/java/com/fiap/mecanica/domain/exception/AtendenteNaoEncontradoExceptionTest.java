package com.fiap.mecanica.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AtendenteNaoEncontradoExceptionTest {

  @Test
  @DisplayName("Deve construir mensagem e código a partir do ID")
  void shouldBuildMessageAndCodeFromId() {
    UUID id = UUID.randomUUID();

    AtendenteNaoEncontradoException ex = new AtendenteNaoEncontradoException(id);

    assertEquals("ATENDENTE_404", ex.getCode());
    assertEquals("Atendente não encontrado com ID: %s".formatted(id), ex.getMessage());
  }

  @Test
  @DisplayName("Deve aceitar mensagem customizada mantendo o código")
  void shouldAcceptCustomMessage() {
    String message = "Atendente não encontrado";

    AtendenteNaoEncontradoException ex = new AtendenteNaoEncontradoException(message);

    assertEquals("ATENDENTE_404", ex.getCode());
    assertEquals(message, ex.getMessage());
  }
}
