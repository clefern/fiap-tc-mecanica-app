package com.fiap.mecanica.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.repository.InsumoRepository;
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
class InsumoServiceImplTest {

  @Mock private InsumoRepository repository;

  @InjectMocks private InsumoServiceImpl service;

  @Test
  @DisplayName("Deve criar insumo com sucesso")
  void shouldCreateInsumo() {
    Insumo insumo = new Insumo(null, "Insumo 1", "Desc 1", BigDecimal.TEN, true, "L", 100, 20, 200);
    Insumo saved =
        new Insumo(
            UUID.randomUUID(), "Insumo 1", "Desc 1", BigDecimal.TEN, true, "L", 100, 20, 200);

    when(repository.save(any(Insumo.class))).thenReturn(saved);

    Insumo result = service.create(insumo);

    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals("Insumo 1", result.getNome());
    verify(repository).save(insumo);
  }

  @Test
  @DisplayName("Deve listar todos os insumos")
  void shouldListAll() {
    Insumo i1 = new Insumo(UUID.randomUUID(), "I1", "D1", BigDecimal.TEN, true, "L", 100, 20, 200);
    Pageable pageable = Pageable.unpaged();
    Page<Insumo> page = new PageImpl<>(List.of(i1));

    when(repository.findAll(pageable)).thenReturn(page);

    Page<Insumo> result = service.getAll(pageable);

    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Deve listar insumos ativos")
  void shouldListActive() {
    Insumo i1 = new Insumo(UUID.randomUUID(), "I1", "D1", BigDecimal.TEN, true, "L", 100, 20, 200);
    Pageable pageable = Pageable.unpaged();
    Page<Insumo> page = new PageImpl<>(List.of(i1));

    when(repository.findByAtivoTrue(pageable)).thenReturn(page);

    Page<Insumo> result = service.getAllAtivos(pageable);

    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldGetById() {
    UUID id = UUID.randomUUID();
    Insumo i1 = new Insumo(id, "I1", "D1", BigDecimal.TEN, true, "L", 100, 20, 200);
    when(repository.findById(id)).thenReturn(Optional.of(i1));

    Optional<Insumo> result = service.getById(id);

    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());
  }

  @Test
  @DisplayName("Deve buscar insumos por termo em nome ou descrição")
  void shouldSearchByTerm() {
    Insumo i1 =
        new Insumo(
            UUID.randomUUID(),
            "Óleo Sintético",
            "Desc óleo",
            BigDecimal.TEN,
            true,
            "L",
            100,
            20,
            200);
    Pageable pageable = PageRequest.of(0, 10);

    when(repository.search("oleo", pageable)).thenReturn(new PageImpl<>(List.of(i1)));

    Page<Insumo> result = service.search("oleo", pageable);

    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Deve atualizar insumo existente")
  void shouldUpdate() {
    UUID id = UUID.randomUUID();
    Insumo existing = new Insumo(id, "Old", "Old", BigDecimal.ONE, true, "Old", 100, 20, 200);
    Insumo updateData = new Insumo(id, "New", "New", BigDecimal.TEN, true, "New", 200, 40, 400);

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

    Insumo result = service.update(id, updateData);

    assertEquals("New", result.getNome());
    assertEquals("New", result.getUnidadeMedida());
    verify(repository).save(existing);
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar insumo inexistente")
  void shouldThrowWhenUpdateNotFound() {
    UUID id = UUID.randomUUID();
    Insumo updateData = new Insumo(id, "New", "New", BigDecimal.TEN, true, "New", 100, 20, 200);

    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.update(id, updateData));
  }

  @Test
  @DisplayName("Deve deletar insumo")
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
    int qtdAtual = 100;
    int qtdEntrada = 50;
    Insumo insumo = new Insumo(id, "I1", "D1", BigDecimal.TEN, true, "L", qtdAtual, 20, 200);

    when(repository.findById(id)).thenReturn(Optional.of(insumo));
    when(repository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

    Insumo result = service.registrarEntradaEstoque(id, qtdEntrada);

    assertEquals(qtdAtual + qtdEntrada, result.getQuantidadeEstoque());
    verify(repository).save(insumo);
  }

  @Test
  @DisplayName("Deve lançar exceção ao registrar entrada em insumo inexistente")
  void shouldRegisterStockEntryNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.registrarEntradaEstoque(id, 50));
  }

  @Test
  @DisplayName("Deve registrar baixa de estoque com sucesso")
  void shouldRegisterStockExitSuccess() {
    UUID id = UUID.randomUUID();
    int qtdAtual = 100;
    int qtdBaixa = 30;
    Insumo insumo = new Insumo(id, "I1", "D1", BigDecimal.TEN, true, "L", qtdAtual, 20, 200);

    when(repository.findById(id)).thenReturn(Optional.of(insumo));
    when(repository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

    Insumo result = service.registrarBaixaEstoque(id, qtdBaixa);

    assertEquals(qtdAtual - qtdBaixa, result.getQuantidadeEstoque());
    verify(repository).save(insumo);
  }

  @Test
  @DisplayName("Deve lançar exceção ao registrar baixa em insumo inexistente")
  void shouldRegisterStockExitNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.registrarBaixaEstoque(id, 30));
  }

  @Test
  @DisplayName("Deve atualizar parâmetros de estoque com sucesso")
  void shouldUpdateStockParametersSuccess() {
    UUID id = UUID.randomUUID();
    Insumo insumo = new Insumo(id, "I1", "D1", BigDecimal.TEN, true, "L", 100, 20, 200);

    when(repository.findById(id)).thenReturn(Optional.of(insumo));
    when(repository.save(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));

    int novoMin = 50;
    int novoMax = 500;
    Insumo result = service.atualizarParametrosEstoque(id, novoMin, novoMax);

    assertEquals(novoMin, result.getEstoqueMinimo());
    assertEquals(novoMax, result.getEstoqueMaximo());
    verify(repository).save(insumo);
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar parâmetros de insumo inexistente")
  void shouldUpdateStockParametersNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class, () -> service.atualizarParametrosEstoque(id, 50, 500));
  }
}
