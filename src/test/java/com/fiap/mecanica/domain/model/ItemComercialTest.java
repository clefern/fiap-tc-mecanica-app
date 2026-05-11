package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemComercialTest {

  private static class ConcreteItemComercial extends ItemComercial {
    public ConcreteItemComercial(
        UUID id, String nome, String descricao, BigDecimal precoBase, boolean ativo) {
      super(id, nome, descricao, precoBase, ativo);
    }
  }

  @Test
  @DisplayName("Deve criar item comercial válido")
  void deveCriarItemValido() {
    UUID id = UUID.randomUUID();
    ItemComercial item = new ConcreteItemComercial(id, "Nome", "Desc", BigDecimal.TEN, true);

    assertEquals(id, item.getId());
    assertEquals("Nome", item.getNome());
    assertEquals("Desc", item.getDescricao());
    assertEquals(BigDecimal.TEN, item.getPrecoBase());
    assertTrue(item.isAtivo());
  }

  @Test
  @DisplayName("Não deve criar item com nome inválido")
  void naoDeveCriarComNomeInvalido() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConcreteItemComercial(UUID.randomUUID(), null, "Desc", BigDecimal.TEN, true));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConcreteItemComercial(UUID.randomUUID(), "", "Desc", BigDecimal.TEN, true));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConcreteItemComercial(UUID.randomUUID(), "   ", "Desc", BigDecimal.TEN, true));
  }

  @Test
  @DisplayName("Não deve criar item com descrição inválida")
  void naoDeveCriarComDescricaoInvalida() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConcreteItemComercial(UUID.randomUUID(), "Nome", null, BigDecimal.TEN, true));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConcreteItemComercial(UUID.randomUUID(), "Nome", "", BigDecimal.TEN, true));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConcreteItemComercial(UUID.randomUUID(), "Nome", "   ", BigDecimal.TEN, true));
  }

  @Test
  @DisplayName("Não deve criar item com preço negativo")
  void naoDeveCriarComPrecoNegativo() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConcreteItemComercial(
                UUID.randomUUID(), "Nome", "Desc", new BigDecimal("-1"), true));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConcreteItemComercial(UUID.randomUUID(), "Nome", "Desc", null, true));
  }

  @Test
  @DisplayName("Deve atualizar preço")
  void deveAtualizarPreco() {
    ItemComercial item =
        new ConcreteItemComercial(UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true);

    item.atualizarPreco(new BigDecimal("20.00"));
    assertEquals(new BigDecimal("20.00"), item.getPrecoBase());
  }

  @Test
  @DisplayName("Não deve atualizar preço com valor inválido")
  void naoDeveAtualizarPrecoInvalido() {
    ItemComercial item =
        new ConcreteItemComercial(UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true);

    assertThrows(IllegalArgumentException.class, () -> item.atualizarPreco(null));
    assertThrows(IllegalArgumentException.class, () -> item.atualizarPreco(new BigDecimal("-1")));
  }

  @Test
  @DisplayName("Deve atualizar dados básicos")
  void deveAtualizarDadosBasicos() {
    ItemComercial item =
        new ConcreteItemComercial(UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true);

    item.atualizarDadosBasicos("Novo Nome", "Nova Desc");
    assertEquals("Novo Nome", item.getNome());
    assertEquals("Nova Desc", item.getDescricao());
  }

  @Test
  @DisplayName("Não deve atualizar dados básicos com nome inválido")
  void naoDeveAtualizarDadosBasicosInvalidos() {
    ItemComercial item =
        new ConcreteItemComercial(UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true);

    assertThrows(IllegalArgumentException.class, () -> item.atualizarDadosBasicos(null, "Desc"));
    assertThrows(IllegalArgumentException.class, () -> item.atualizarDadosBasicos("", "Desc"));
  }

  @Test
  @DisplayName("Deve ativar e inativar")
  void deveAtivarInativar() {
    ItemComercial item =
        new ConcreteItemComercial(UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true);

    item.inativar();
    assertFalse(item.isAtivo());

    item.ativar();
    assertTrue(item.isAtivo());
  }
}
