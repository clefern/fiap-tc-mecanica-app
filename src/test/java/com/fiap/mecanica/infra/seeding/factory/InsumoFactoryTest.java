package com.fiap.mecanica.infra.seeding.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fiap.mecanica.domain.model.Insumo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InsumoFactoryTest {

  private final InsumoFactory factory = new InsumoFactory();

  @Test
  @DisplayName("Should create single Insumo")
  void shouldCreateSingleInsumo() {
    Insumo insumo = factory.create();
    assertNotNull(insumo);
    assertNotNull(insumo.getNome());
    assertNotNull(insumo.getDescricao());
    assertNotNull(insumo.getPrecoBase());
    assertNotNull(insumo.getUnidadeMedida());
    assertTrue(insumo.isAtivo());
  }

  @Test
  @DisplayName("Should create all templates")
  void shouldCreateAllTemplates() {
    List<Insumo> insumos = factory.createAllTemplates();
    assertNotNull(insumos);
    assertFalse(insumos.isEmpty());
    // There are 30 templates in the code
    assertEquals(30, insumos.size());

    insumos.forEach(
        i -> {
          assertNotNull(i.getNome());
          assertNotNull(i.getDescricao());
          assertNotNull(i.getPrecoBase());
          assertTrue(i.isAtivo());
        });
  }

  @Test
  @DisplayName("Should create many insumos")
  void shouldCreateManyInsumos() {
    List<Insumo> insumos = factory.createMany(5);
    assertEquals(5, insumos.size());
  }
}
