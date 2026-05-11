package com.fiap.mecanica.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.repository.PecaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PecaServiceImplTest {

  @Mock private PecaRepository repository;

  @InjectMocks private PecaServiceImpl service;

  @Test
  @DisplayName("Deve criar peça com sucesso")
  void shouldCreatePeca() {
    Peca peca =
        new Peca(null, "Peca 1", "Desc 1", BigDecimal.TEN, true, "Fab", "Cod", "Mod", 10, 5, 50);
    Peca saved =
        new Peca(
            UUID.randomUUID(),
            "Peca 1",
            "Desc 1",
            BigDecimal.TEN,
            true,
            "Fab",
            "Cod",
            "Mod",
            10,
            5,
            50);

    when(repository.save(any(Peca.class))).thenReturn(saved);

    Peca result = service.create(peca);

    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals("Peca 1", result.getNome());
    verify(repository).save(peca);
  }

  @Test
  @DisplayName("Deve listar todas as peças")
  void shouldListAll() {
    Peca p1 =
        new Peca(UUID.randomUUID(), "P1", "D1", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50);
    Pageable pageable = PageRequest.of(0, 10);
    when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(p1)));

    Page<Peca> result = service.getAll(pageable);

    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Deve listar peças ativas")
  void shouldListActive() {
    Peca p1 =
        new Peca(UUID.randomUUID(), "P1", "D1", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50);
    Pageable pageable = PageRequest.of(0, 10);
    when(repository.findByAtivoTrue(pageable)).thenReturn(new PageImpl<>(List.of(p1)));

    Page<Peca> result = service.getAllAtivos(pageable);

    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Deve buscar peças por termo em nome ou descrição")
  void shouldSearchByTerm() {
    Peca p1 =
        new Peca(
            UUID.randomUUID(),
            "Filtro de Óleo",
            "Desc filtro",
            BigDecimal.TEN,
            true,
            "F",
            "C",
            "M",
            10,
            5,
            50);
    Pageable pageable = PageRequest.of(0, 10);

    when(repository.search("filtro", pageable)).thenReturn(new PageImpl<>(List.of(p1)));

    Page<Peca> result = service.search("filtro", pageable);

    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldGetById() {
    UUID id = UUID.randomUUID();
    Peca p1 = new Peca(id, "P1", "D1", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50);
    when(repository.findById(id)).thenReturn(Optional.of(p1));

    Optional<Peca> result = service.getById(id);

    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());
  }

  @Test
  @DisplayName("Deve atualizar peça existente")
  void shouldUpdate() {
    UUID id = UUID.randomUUID();
    Peca existing =
        new Peca(id, "Old", "Old", BigDecimal.ONE, true, "Old", "Old", "Old", 10, 5, 50);
    Peca updateData =
        new Peca(id, "New", "New", BigDecimal.TEN, true, "New", "New", "New", 20, 10, 100);

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.save(any(Peca.class))).thenAnswer(i -> i.getArgument(0));

    Peca result = service.update(id, updateData);

    assertEquals("New", result.getNome());
    assertEquals("New", result.getFabricante());
    verify(repository).save(existing);
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar peça inexistente")
  void shouldThrowWhenUpdateNotFound() {
    UUID id = UUID.randomUUID();
    Peca updateData =
        new Peca(id, "New", "New", BigDecimal.TEN, true, "New", "New", "New", 20, 10, 100);

    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.update(id, updateData));
  }

  @Test
  @DisplayName("Deve deletar peça")
  void shouldDelete() {
    UUID id = UUID.randomUUID();
    doNothing().when(repository).delete(id);

    service.delete(id);

    verify(repository).delete(id);
  }

  @Test
  @DisplayName("Deve registrar entrada de estoque com sucesso")
  void shouldRegisterStockEntrySuccess() {
    UUID id = UUID.randomUUID();
    int qtdAtual = 10;
    int qtdEntrada = 5;
    Peca peca = new Peca(id, "P1", "D1", BigDecimal.TEN, true, "F", "C", "M", qtdAtual, 5, 50);

    when(repository.findById(id)).thenReturn(Optional.of(peca));
    when(repository.save(any(Peca.class))).thenAnswer(i -> i.getArgument(0));

    Peca result = service.registrarEntradaEstoque(id, qtdEntrada);

    assertEquals(qtdAtual + qtdEntrada, result.getQuantidadeEstoque());
    verify(repository).save(peca);
  }

  @Test
  @DisplayName("Deve lançar exceção ao registrar entrada em peça inexistente")
  void shouldRegisterStockEntryNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.registrarEntradaEstoque(id, 10));
  }

  @Test
  @DisplayName("Deve registrar baixa de estoque com sucesso")
  void shouldRegisterStockExitSuccess() {
    UUID id = UUID.randomUUID();
    int qtdAtual = 10;
    int qtdBaixa = 3;
    Peca peca = new Peca(id, "P1", "D1", BigDecimal.TEN, true, "F", "C", "M", qtdAtual, 5, 50);

    when(repository.findById(id)).thenReturn(Optional.of(peca));
    when(repository.save(any(Peca.class))).thenAnswer(i -> i.getArgument(0));

    Peca result = service.registrarBaixaEstoque(id, qtdBaixa);

    assertEquals(qtdAtual - qtdBaixa, result.getQuantidadeEstoque());
    verify(repository).save(peca);
  }

  @Test
  @DisplayName("Deve lançar exceção ao registrar baixa em peça inexistente")
  void shouldRegisterStockExitNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.registrarBaixaEstoque(id, 5));
  }

  @Test
  @DisplayName("Deve atualizar parâmetros de estoque com sucesso")
  void shouldUpdateStockParametersSuccess() {
    UUID id = UUID.randomUUID();
    Peca peca = new Peca(id, "P1", "D1", BigDecimal.TEN, true, "F", "C", "M", 10, 5, 50);

    when(repository.findById(id)).thenReturn(Optional.of(peca));
    when(repository.save(any(Peca.class))).thenAnswer(i -> i.getArgument(0));

    int novoMin = 15;
    int novoMax = 100;
    Peca result = service.atualizarParametrosEstoque(id, novoMin, novoMax);

    assertEquals(novoMin, result.getEstoqueMinimo());
    assertEquals(novoMax, result.getEstoqueMaximo());
    verify(repository).save(peca);
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar parâmetros de peça inexistente")
  void shouldUpdateStockParametersNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class, () -> service.atualizarParametrosEstoque(id, 10, 20));
  }
}
