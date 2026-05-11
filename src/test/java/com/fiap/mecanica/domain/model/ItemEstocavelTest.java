package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.enums.StatusEstoque;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemEstocavelTest {

  private static class ConcreteItemEstocavel extends ItemEstocavel {
    public ConcreteItemEstocavel(
        UUID id,
        String nome,
        String descricao,
        BigDecimal precoBase,
        boolean ativo,
        Integer quantidadeEstoque,
        Integer estoqueMinimo,
        Integer estoqueMaximo) {
      super(id, nome, descricao, precoBase, ativo, quantidadeEstoque, estoqueMinimo, estoqueMaximo);
    }
  }

  @Test
  @DisplayName("Deve permitir estoque minimo maior que maximo se maximo for zero")
  void devePermitirMinimoMaiorQueMaximoSeMaximoZero() {
    assertDoesNotThrow(
        () -> {
          new ConcreteItemEstocavel(
              UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 20, 0);
        });
  }

  @Test
  @DisplayName("Deve atualizar estoque permitindo minimo maior que maximo se maximo for zero")
  void deveAtualizarPermitindoMinimoMaiorQueMaximoSeMaximoZero() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);

    assertDoesNotThrow(() -> item.atualizarEstoque(null, 20, 0));
    assertEquals(20, item.getEstoqueMinimo());
    assertEquals(0, item.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve criar item estocável válido")
  void deveCriarItemValido() {
    UUID id = UUID.randomUUID();
    ItemEstocavel item =
        new ConcreteItemEstocavel(id, "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);

    assertEquals(id, item.getId());
    assertEquals("Nome", item.getNome());
    assertEquals("Desc", item.getDescricao());
    assertEquals(BigDecimal.TEN, item.getPrecoBase());
    assertEquals(10, item.getQuantidadeEstoque());
    assertEquals(5, item.getEstoqueMinimo());
    assertEquals(50, item.getEstoqueMaximo());
    assertTrue(item.isAtivo());
  }

  @Test
  @DisplayName("Não deve criar item com estoque inválido")
  void naoDeveCriarComEstoqueInvalido() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConcreteItemEstocavel(
                UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, -1, 5, 50));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConcreteItemEstocavel(
                UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, -1, 50));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConcreteItemEstocavel(
                UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, -1));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConcreteItemEstocavel(
                UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 50, 5));
  }

  @Test
  @DisplayName("Deve inicializar com zeros se valores nulos no construtor")
  void deveInicializarComZerosSeNulos() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, null, null, null);

    assertEquals(0, item.getQuantidadeEstoque());
    assertEquals(0, item.getEstoqueMinimo());
    assertEquals(0, item.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Não deve baixar estoque negativo ou zero")
  void naoDeveBaixarEstoqueNegativoOuZero() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);
    assertThrows(IllegalArgumentException.class, () -> item.baixarEstoque(0));
    assertThrows(IllegalArgumentException.class, () -> item.baixarEstoque(-1));
  }

  @Test
  @DisplayName("Deve identificar status RUPTURA")
  void deveIdentificarRuptura() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 0, 5, 50);
    assertEquals(StatusEstoque.RUPTURA, item.verificarStatusEstoque());
  }

  @Test
  @DisplayName("Deve identificar status CRITICO")
  void deveIdentificarCritico() {
    // Quantidade <= Mínimo (mas > 0)
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 5, 5, 50);
    assertEquals(StatusEstoque.CRITICO, item.verificarStatusEstoque());

    item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 1, 5, 50);
    assertEquals(StatusEstoque.CRITICO, item.verificarStatusEstoque());
  }

  @Test
  @DisplayName("Deve identificar status PRE_ALERTA")
  void deveIdentificarPreAlerta() {
    // Mínimo = 10, Máximo = 110. Diferença = 100. Margem = 10% de 100 = 10.
    // Limite pré-alerta = Mínimo + Margem = 10 + 10 = 20.
    // Quantidade deve ser > 10 e <= 20.
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 20, 10, 110);
    assertEquals(StatusEstoque.PRE_ALERTA, item.verificarStatusEstoque());

    item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 11, 10, 110);
    assertEquals(StatusEstoque.PRE_ALERTA, item.verificarStatusEstoque());
  }

  @Test
  @DisplayName("Deve identificar status NORMAL")
  void deveIdentificarNormal() {
    // Mínimo = 10, Máximo = 110. Limite pré-alerta = 20.
    // Quantidade > 20.
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 21, 10, 110);
    assertEquals(StatusEstoque.NORMAL, item.verificarStatusEstoque());

    // Teste com estoque máximo zerado (ignora pré-alerta)
    item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 11, 10, 0);
    assertEquals(StatusEstoque.NORMAL, item.verificarStatusEstoque());
  }

  @Test
  @DisplayName("Deve baixar estoque")
  void deveBaixarEstoque() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);
    item.baixarEstoque(5);
    assertEquals(5, item.getQuantidadeEstoque());
  }

  @Test
  @DisplayName("Não deve baixar estoque insuficiente")
  void naoDeveBaixarEstoqueInsuficiente() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 5, 5, 50);
    assertThrows(IllegalStateException.class, () -> item.baixarEstoque(6));
  }

  @Test
  void deveRetornarStatusCriticoQuandoEstoqueIgualAoMinimo() {
    // Arrange
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 5, 5, 20);

    // Act
    StatusEstoque status = item.verificarStatusEstoque();

    // Assert
    assertEquals(StatusEstoque.CRITICO, status);
  }

  @Test
  void deveRetornarStatusPreAlertaQuandoEstoqueNaMargemDe10Porcento() {
    // Arrange
    // Min=10, Max=110. Diff=100. 10%=10. Threshold = 10 + 10 = 20.
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 20, 10, 110);

    // Act
    StatusEstoque status = item.verificarStatusEstoque();

    // Assert
    assertEquals(StatusEstoque.PRE_ALERTA, status);
  }

  @Test
  void deveAplicarConsumerDePecaQuandoItemForPeca() {
    // Arrange
    Peca peca =
        new Peca(
            UUID.randomUUID(),
            "Peca",
            "Desc",
            BigDecimal.TEN,
            true,
            "Fab",
            "COD",
            "Mod",
            10,
            5,
            50);
    AtomicBoolean pecaConsumerCalled = new AtomicBoolean(false);
    AtomicBoolean insumoConsumerCalled = new AtomicBoolean(false);

    // Act
    peca.aplicarPorTipo(p -> pecaConsumerCalled.set(true), i -> insumoConsumerCalled.set(true));

    // Assert
    assertTrue(pecaConsumerCalled.get());
    assertFalse(insumoConsumerCalled.get());
  }

  @Test
  void deveAplicarConsumerDeInsumoQuandoItemForInsumo() {
    // Arrange
    Insumo insumo =
        new Insumo(UUID.randomUUID(), "Insumo", "Desc", BigDecimal.TEN, true, "UN", 10, 5, 50);
    AtomicBoolean pecaConsumerCalled = new AtomicBoolean(false);
    AtomicBoolean insumoConsumerCalled = new AtomicBoolean(false);

    // Act
    insumo.aplicarPorTipo(p -> pecaConsumerCalled.set(true), i -> insumoConsumerCalled.set(true));

    // Assert
    assertFalse(pecaConsumerCalled.get());
    assertTrue(insumoConsumerCalled.get());
  }

  @Test
  @DisplayName("Deve retornar status NORMAL quando estoque maximo igual ao minimo")
  void deveRetornarNormalQuandoMaximoIgualMinimo() {
    // Arrange
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 11, 10, 10);

    // Act
    StatusEstoque status = item.verificarStatusEstoque();

    // Assert
    assertEquals(StatusEstoque.NORMAL, status);
  }

  @Test
  @DisplayName("Nao deve aplicar nenhum consumer quando item nao for Peca nem Insumo")
  void naoDeveAplicarConsumerQuandoItemDesconhecido() {
    // Arrange
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);
    AtomicBoolean pecaConsumerCalled = new AtomicBoolean(false);
    AtomicBoolean insumoConsumerCalled = new AtomicBoolean(false);

    // Act
    item.aplicarPorTipo(p -> pecaConsumerCalled.set(true), i -> insumoConsumerCalled.set(true));

    // Assert
    assertFalse(pecaConsumerCalled.get());
    assertFalse(insumoConsumerCalled.get());
  }

  @Test
  @DisplayName("Deve lancar excecao quando estoque minimo maior que maximo")
  void deveLancarExcecaoQuandoMinimoMaiorQueMaximo() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new ConcreteItemEstocavel(
              UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 20, 10);
        });
  }

  @Test
  @DisplayName("Deve adicionar estoque")
  void deveAdicionarEstoque() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);
    item.adicionarEstoque(5);
    assertEquals(15, item.getQuantidadeEstoque());
  }

  @Test
  @DisplayName("Nao deve adicionar estoque negativo ou zero")
  void naoDeveAdicionarEstoqueNegativoOuZero() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);
    assertThrows(IllegalArgumentException.class, () -> item.adicionarEstoque(0));
    assertThrows(IllegalArgumentException.class, () -> item.adicionarEstoque(-1));
  }

  @Test
  @DisplayName("Deve atualizar estoque corretamente")
  void deveAtualizarEstoque() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);

    item.atualizarEstoque(20, 10, 60);

    assertEquals(20, item.getQuantidadeEstoque());
    assertEquals(10, item.getEstoqueMinimo());
    assertEquals(60, item.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve atualizar apenas campos fornecidos")
  void deveAtualizarApenasCamposFornecidos() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);

    item.atualizarEstoque(null, 10, null);

    assertEquals(10, item.getQuantidadeEstoque());
    assertEquals(10, item.getEstoqueMinimo());
    assertEquals(50, item.getEstoqueMaximo());
  }

  @Test
  @DisplayName("Deve lancar excecao ao atualizar com valores invalidos")
  void deveLancarExcecaoAoAtualizarComValoresInvalidos() {
    ItemEstocavel item =
        new ConcreteItemEstocavel(
            UUID.randomUUID(), "Nome", "Desc", BigDecimal.TEN, true, 10, 5, 50);

    assertThrows(IllegalArgumentException.class, () -> item.atualizarEstoque(-1, null, null));
    assertThrows(IllegalArgumentException.class, () -> item.atualizarEstoque(null, -1, null));
    assertThrows(IllegalArgumentException.class, () -> item.atualizarEstoque(null, null, -1));
    assertThrows(
        IllegalArgumentException.class, () -> item.atualizarEstoque(null, 20, 10)); // Min > Max
  }
}
