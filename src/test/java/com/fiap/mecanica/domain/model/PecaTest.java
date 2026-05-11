package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PecaTest {

  @Test
  @DisplayName("Deve criar peça válida")
  void deveCriarPecaValida() {
    UUID id = UUID.randomUUID();
    Peca peca =
        new Peca(
            id,
            "Filtro de Ar",
            "Filtro de ar esportivo",
            new BigDecimal("150.00"),
            true,
            "K&N",
            "KN-1234",
            "Esportivo",
            50,
            10,
            100);

    assertEquals(id, peca.getId());
    assertEquals("Filtro de Ar", peca.getNome());
    assertEquals("Filtro de ar esportivo", peca.getDescricao());
    assertEquals(new BigDecimal("150.00"), peca.getPrecoBase());
    assertTrue(peca.isAtivo());
    assertEquals("K&N", peca.getFabricante());
    assertEquals("KN-1234", peca.getCodigoFabricante());
    assertEquals("Esportivo", peca.getModelo());
    assertEquals(50, peca.getQuantidadeEstoque());
    assertEquals(10, peca.getEstoqueMinimo());
    assertEquals(100, peca.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Não deve criar peça sem nome")
  void naoDeveCriarPecaSemNome() {
    UUID id = UUID.randomUUID();
    assertThrows(
        IllegalArgumentException.class,
        () -> new Peca(id, null, "Desc", BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Peca(id, "", "Desc", BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Peca(id, "   ", "Desc", BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50));
  }

  @Test
  @DisplayName("Não deve criar peça sem descrição")
  void naoDeveCriarPecaSemDescricao() {
    UUID id = UUID.randomUUID();
    assertThrows(
        IllegalArgumentException.class,
        () -> new Peca(id, "Nome", null, BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Peca(id, "Nome", "", BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Peca(id, "Nome", "   ", BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50));
  }

  @Test
  @DisplayName("Não deve criar peça com preço inválido")
  void naoDeveCriarPecaComPrecoInvalido() {
    UUID id = UUID.randomUUID();
    assertThrows(
        IllegalArgumentException.class,
        () -> new Peca(id, "Nome", "Desc", null, true, "Fab", "Cod", "Mod", 10, 5, 50));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Peca(
                id, "Nome", "Desc", new BigDecimal("-1"), true, "Fab", "Cod", "Mod", 10, 5, 50));
  }

  @Test
  @DisplayName("Deve atualizar peça")
  void deveAtualizarPeca() {
    UUID id = UUID.randomUUID();
    Peca peca =
        new Peca(
            id, "Original", "Desc Original", BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50);

    peca.atualizar(
        "Novo",
        "Nova Desc",
        BigDecimal.ONE,
        false,
        "Novo Fab",
        "Novo Cod",
        "Novo Mod",
        20,
        10,
        100);

    assertEquals("Novo", peca.getNome());
    assertEquals("Nova Desc", peca.getDescricao());
    assertEquals(BigDecimal.ONE, peca.getPrecoBase());
    assertFalse(peca.isAtivo());
    assertEquals("Novo Fab", peca.getFabricante());
    assertEquals("Novo Cod", peca.getCodigoFabricante());
    assertEquals("Novo Mod", peca.getModelo());
    assertEquals(20, peca.getQuantidadeEstoque());
    assertEquals(10, peca.getEstoqueMinimo());
  }

  @Test
  @DisplayName("Não deve atualizar peça com dados inválidos")
  void naoDeveAtualizarPecaComDadosInvalidos() {
    UUID id = UUID.randomUUID();
    Peca peca =
        new Peca(
            id, "Original", "Desc Original", BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50);

    assertThrows(
        IllegalArgumentException.class,
        () -> peca.atualizar(null, "Desc", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50));
    assertThrows(
        IllegalArgumentException.class,
        () -> peca.atualizar("", "Desc", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50));
  }

  @Test
  @DisplayName("Não deve atualizar preço com valor inválido")
  void naoDeveAtualizarPrecoInvalido() {
    Peca peca =
        new Peca(UUID.randomUUID(), "N", "D", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50);

    assertThrows(IllegalArgumentException.class, () -> peca.atualizarPreco(null));
    assertThrows(IllegalArgumentException.class, () -> peca.atualizarPreco(new BigDecimal("-1")));
  }

  @Test
  @DisplayName("Deve testar equals, hashCode e toString")
  void deveTestarMetodosBasicos() {
    UUID id = UUID.randomUUID();
    Peca p1 = new Peca(id, "A", "D", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50);
    Peca p2 = new Peca(id, "B", "E", BigDecimal.ONE, false, "G", "H", "I", 20, 10, 100);
    Peca p3 = new Peca(UUID.randomUUID(), "A", "D", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50);

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1, p3);
    assertNotNull(p1.toString());
  }
}
