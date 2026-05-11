package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InsumoTest {

  @Test
  @DisplayName("Deve criar insumo válido")
  void deveCriarInsumoValido() {
    UUID id = UUID.randomUUID();
    Insumo insumo =
        new Insumo(
            id,
            "Óleo 10W40",
            "Óleo sintético",
            new BigDecimal("50.00"),
            true,
            "Litros",
            100,
            20,
            200);

    assertEquals(id, insumo.getId());
    assertEquals("Óleo 10W40", insumo.getNome());
    assertEquals("Óleo sintético", insumo.getDescricao());
    assertEquals(new BigDecimal("50.00"), insumo.getPrecoBase());
    assertTrue(insumo.isAtivo());
    assertEquals("Litros", insumo.getUnidadeMedida());
    assertEquals(100, insumo.getQuantidadeEstoque());
    assertEquals(20, insumo.getEstoqueMinimo());
    assertEquals(200, insumo.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Não deve criar insumo sem unidade de medida")
  void naoDeveCriarInsumoSemUnidadeMedida() {
    UUID id = UUID.randomUUID();
    assertThrows(
        IllegalArgumentException.class,
        () -> new Insumo(id, "Nome", "Desc", BigDecimal.TEN, true, null, 0, 0, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Insumo(id, "Nome", "Desc", BigDecimal.TEN, true, "", 0, 0, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Insumo(id, "Nome", "Desc", BigDecimal.TEN, true, "   ", 0, 0, 0));
  }

  @Test
  @DisplayName("Deve atualizar insumo")
  void deveAtualizarInsumo() {
    UUID id = UUID.randomUUID();
    Insumo insumo =
        new Insumo(id, "Original", "Desc Original", BigDecimal.TEN, true, "L", 10, 5, 50);

    insumo.atualizar("Novo", "Desc Nova", BigDecimal.ONE, false, "ML", 20, 10, 100);

    assertEquals("Novo", insumo.getNome());
    assertEquals("Desc Nova", insumo.getDescricao());
    assertEquals(BigDecimal.ONE, insumo.getPrecoBase());
    assertFalse(insumo.isAtivo());
    assertEquals("ML", insumo.getUnidadeMedida());
    assertEquals(20, insumo.getQuantidadeEstoque());
    assertEquals(10, insumo.getEstoqueMinimo());
    assertEquals(100, insumo.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Não deve atualizar insumo com dados inválidos")
  void naoDeveAtualizarInsumoComDadosInvalidos() {
    UUID id = UUID.randomUUID();
    Insumo insumo =
        new Insumo(id, "Original", "Desc Original", BigDecimal.TEN, true, "L", 10, 5, 50);

    assertThrows(
        IllegalArgumentException.class,
        () -> insumo.atualizar("Novo", "Desc", BigDecimal.ONE, true, null, 10, 5, 50));
  }

  @Test
  @DisplayName("Deve testar equals, hashCode e toString")
  void deveTestarMetodosBasicos() {
    UUID id = UUID.randomUUID();
    Insumo i1 = new Insumo(id, "A", "D", BigDecimal.TEN, true, "L", 0, 0, 0);
    Insumo i2 = new Insumo(id, "B", "D", BigDecimal.TEN, true, "L", 0, 0, 0);
    Insumo i3 = new Insumo(UUID.randomUUID(), "A", "D", BigDecimal.TEN, true, "L", 0, 0, 0);

    assertEquals(i1, i2);
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotEquals(i1, i3);
    assertNotNull(i1.toString());
  }
}
