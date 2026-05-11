package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrcamentoTest {

  @Test
  @DisplayName("Deve aprovar orçamento quando status for GERADO")
  void deveAprovarOrcamentoGerado() {
    Orcamento orcamento =
        Orcamento.builder().id(UUID.randomUUID()).status(StatusOrcamento.GERADO).build();

    orcamento.aprovar();

    assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
  }

  @Test
  @DisplayName("Não deve aprovar orçamento se status não for GERADO")
  void naoDeveAprovarOrcamentoNaoGerado() {
    Orcamento orcamento =
        Orcamento.builder().id(UUID.randomUUID()).status(StatusOrcamento.CANCELADO).build();

    IllegalStateException exception = assertThrows(IllegalStateException.class, orcamento::aprovar);
    assertEquals("Apenas orçamentos gerados podem ser aprovados.", exception.getMessage());
  }

  @Test
  @DisplayName("Deve reprovar orçamento quando status for GERADO")
  void deveReprovarOrcamentoGerado() {
    Orcamento orcamento =
        Orcamento.builder().id(UUID.randomUUID()).status(StatusOrcamento.GERADO).build();

    orcamento.reprovar();

    assertEquals(StatusOrcamento.REJEITADO, orcamento.getStatus());
  }

  @Test
  @DisplayName("Não deve reprovar orçamento se status não for GERADO")
  void naoDeveReprovarOrcamentoNaoGerado() {
    Orcamento orcamento =
        Orcamento.builder().id(UUID.randomUUID()).status(StatusOrcamento.APROVADO).build();

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, orcamento::reprovar);
    assertEquals("Apenas orçamentos gerados podem ser reprovados.", exception.getMessage());
  }

  @Test
  @DisplayName("Deve cancelar orçamento")
  void deveCancelarOrcamento() {
    Orcamento orcamento =
        Orcamento.builder().id(UUID.randomUUID()).status(StatusOrcamento.GERADO).build();

    orcamento.cancelar();

    assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
  }
}
