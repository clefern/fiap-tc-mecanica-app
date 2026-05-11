package com.fiap.mecanica.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstoqueInsuficienteExceptionTest {

  @Test
  @DisplayName("Deve construir mensagem usando ID da peça")
  void shouldBuildMessageFromPecaId() {
    UUID pecaId = UUID.randomUUID();

    EstoqueInsuficienteException ex = new EstoqueInsuficienteException(pecaId, 10, 5);

    assertEquals("STK-422-01", ex.getCode());
    assertEquals(
        "Estoque insuficiente para o item '%s'. Solicitado: %d, Disponível: %d"
            .formatted(pecaId, 10, 5),
        ex.getMessage());
  }

  @Test
  @DisplayName("Deve construir mensagem usando nome do item")
  void shouldBuildMessageFromItemName() {
    EstoqueInsuficienteException ex = new EstoqueInsuficienteException("Pneu", 3, 1);

    assertEquals("STK-422-01", ex.getCode());
    assertEquals(
        "Estoque insuficiente para o item '%s'. Solicitado: %d, Disponível: %d"
            .formatted("Pneu", 3, 1),
        ex.getMessage());
  }
}
