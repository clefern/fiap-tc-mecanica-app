package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServicoTest {

  @Test
  @DisplayName("Deve criar serviço válido")
  void deveCriarServicoValido() {
    Servico servico =
        new Servico(
            "Troca de Óleo",
            "Troca de óleo completa",
            new BigDecimal("150.00"),
            Duration.ofMinutes(30),
            CategoriaServico.MANUTENCAO_PREVENTIVA);

    assertNotNull(servico.getId());
    assertEquals("Troca de Óleo", servico.getNome());
    assertTrue(servico.isAtivo());
    assertEquals(CategoriaServico.MANUTENCAO_PREVENTIVA, servico.getCategoria());
  }

  @Test
  @DisplayName("Não deve criar serviço sem nome")
  void naoDeveCriarServicoSemNome() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Servico(
                null, "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Servico(
                "", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Servico(
                "   ", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS));
  }

  @Test
  @DisplayName("Não deve criar serviço sem descrição")
  void naoDeveCriarServicoSemDescricao() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Servico(
                "Nome", null, BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Servico(
                "Nome", "", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Servico(
                "Nome", "   ", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS));
  }

  @Test
  @DisplayName("Não deve criar serviço com valor inválido")
  void naoDeveCriarServicoComValorInvalido() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Servico("Nome", "Desc", null, Duration.ofMinutes(30), CategoriaServico.OUTROS));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Servico(
                "Nome",
                "Desc",
                new BigDecimal("-10.00"),
                Duration.ofMinutes(30),
                CategoriaServico.OUTROS));
  }

  @Test
  @DisplayName("Não deve criar serviço com tempo estimado inválido")
  void naoDeveCriarServicoComTempoInvalido() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Servico("Nome", "Desc", BigDecimal.TEN, null, CategoriaServico.OUTROS));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Servico(
                "Nome", "Desc", BigDecimal.TEN, Duration.ofMinutes(-10), CategoriaServico.OUTROS));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Servico("Nome", "Desc", BigDecimal.TEN, Duration.ZERO, CategoriaServico.OUTROS));
  }

  @Test
  @DisplayName("Não deve criar serviço sem categoria")
  void naoDeveCriarServicoSemCategoria() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Servico("Nome", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), null));
  }

  @Test
  @DisplayName("Deve atualizar serviço")
  void deveAtualizarServico() {
    Servico servico =
        new Servico(
            "Original",
            "Original Desc",
            BigDecimal.TEN,
            Duration.ofMinutes(30),
            CategoriaServico.OUTROS);

    servico.atualizar(
        "Novo",
        "Nova Desc",
        BigDecimal.ONE,
        Duration.ofMinutes(60),
        CategoriaServico.REPARO_MECANICO,
        false);

    assertEquals("Novo", servico.getNome());
    assertEquals("Nova Desc", servico.getDescricao());
    assertEquals(BigDecimal.ONE, servico.getPrecoBase());
    assertEquals(Duration.ofMinutes(60), servico.getTempoEstimado());
    assertEquals(CategoriaServico.REPARO_MECANICO, servico.getCategoria());
    assertFalse(servico.isAtivo());
  }

  @Test
  @DisplayName("Deve testar equals e hashCode")
  void deveTestarEqualsHashCode() {
    UUID id = UUID.randomUUID();
    Servico s1 =
        new Servico(
            id, "A", "Desc", BigDecimal.TEN, true, Duration.ofMinutes(30), CategoriaServico.OUTROS);

    Servico s2 =
        new Servico(
            id, "B", "Desc", BigDecimal.TEN, true, Duration.ofMinutes(30), CategoriaServico.OUTROS);

    Servico s3 =
        new Servico(
            UUID.randomUUID(),
            "A",
            "Desc",
            BigDecimal.TEN,
            true,
            Duration.ofMinutes(30),
            CategoriaServico.OUTROS);

    assertEquals(s1, s2);
    assertEquals(s1.hashCode(), s2.hashCode());
    assertNotEquals(s1, s3);
    assertNotEquals(s1, null);
    assertNotEquals(s1, new Object());
  }

  @Test
  @DisplayName("Deve criar serviço com ID e ativo por padrão")
  void deveCriarServicoComIdEAtivoPorPadrao() {
    UUID id = UUID.randomUUID();
    Servico servico =
        new Servico(
            id, "Nome", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS);

    assertEquals(id, servico.getId());
    assertEquals("Nome", servico.getNome());
    assertTrue(servico.isAtivo());
  }

  @Test
  @DisplayName("Deve testar toString")
  void deveTestarToString() {
    Servico servico =
        new Servico(
            "Nome", "Desc", BigDecimal.TEN, Duration.ofMinutes(30), CategoriaServico.OUTROS);
    assertNotNull(servico.toString());
  }
}
