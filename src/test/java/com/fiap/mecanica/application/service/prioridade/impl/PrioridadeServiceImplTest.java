package com.fiap.mecanica.application.service.prioridade.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.domain.exception.ViolacaoPrioridadeException;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import java.util.Collections;
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
class PrioridadeServiceImplTest {

  @Mock private OrdemServicoRepository repository;

  @InjectMocks private PrioridadeServiceImpl service;

  @Test
  @DisplayName("Deve listar fila de orçamento via PrioridadeService")
  void deveListarFilaOrcamento() {
    Pageable pageable = PageRequest.of(0, 10);
    when(repository.listarFilaOrcamento(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    Page<OrdemServico> result = service.listarFilaOrcamento(pageable);

    assertNotNull(result);
    verify(repository).listarFilaOrcamento(any(Pageable.class));
  }

  @Test
  @DisplayName("Deve listar fila de execução via PrioridadeService")
  void deveListarFilaExecucao() {
    Pageable pageable = PageRequest.of(0, 10);
    when(repository.listarFilaExecucao(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    Page<OrdemServico> result = service.listarFilaExecucao(pageable);

    assertNotNull(result);
    verify(repository).listarFilaExecucao(any(Pageable.class));
  }

  @Test
  @DisplayName("Deve obter próxima para orçamento")
  void deveObterProximaParaOrcamento() {
    OrdemServico os = new OrdemServico();
    os.setId(UUID.randomUUID());
    when(repository.listarFilaOrcamento(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(os)));

    Optional<OrdemServico> result = service.obterProximaParaOrcamento();

    assertEquals(Optional.of(os), result);
  }

  @Test
  @DisplayName("Deve obter próxima para execução")
  void deveObterProximaParaExecucao() {
    OrdemServico os = new OrdemServico();
    os.setId(UUID.randomUUID());
    when(repository.listarFilaExecucao(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(os)));

    Optional<OrdemServico> result = service.obterProximaParaExecucao();

    assertEquals(Optional.of(os), result);
  }

  @Test
  @DisplayName("Deve validar prioridade de orçamento com sucesso quando fila vazia")
  void deveValidarPrioridadeOrcamentoFilaVazia() {
    when(repository.listarFilaOrcamento(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    assertDoesNotThrow(() -> service.validarPrioridadeOrcamento(UUID.randomUUID()));
  }

  @Test
  @DisplayName("Deve validar prioridade de orçamento com sucesso quando é a próxima")
  void deveValidarPrioridadeOrcamentoQuandoProxima() {
    UUID id = UUID.randomUUID();
    OrdemServico os = new OrdemServico();
    os.setId(id);

    when(repository.listarFilaOrcamento(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(os)));

    assertDoesNotThrow(() -> service.validarPrioridadeOrcamento(id));
  }

  @Test
  @DisplayName("Deve lançar exceção quando validar prioridade de orçamento e não for a próxima")
  void deveLancarExcecaoValidarPrioridadeOrcamento() {
    UUID idSolicitado = UUID.randomUUID();
    UUID idEsperado = UUID.randomUUID();

    OrdemServico osEsperada = new OrdemServico();
    osEsperada.setId(idEsperado);
    osEsperada.setCodigo("OS-123");
    osEsperada.setPrioridade(Prioridade.BAIXA);

    when(repository.listarFilaOrcamento(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(osEsperada)));

    ViolacaoPrioridadeException ex =
        assertThrows(
            ViolacaoPrioridadeException.class,
            () -> service.validarPrioridadeOrcamento(idSolicitado));

    assertEquals(
        "Violação de prioridade na fila de orçamento. A OS %s deveria ser processada antes da %s."
            .formatted("OS-123", idSolicitado),
        ex.getMessage());
  }

  @Test
  @DisplayName("Deve validar prioridade de execução com sucesso quando fila vazia")
  void deveValidarPrioridadeExecucaoFilaVazia() {
    when(repository.listarFilaExecucao(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    assertDoesNotThrow(() -> service.validarPrioridadeExecucao(UUID.randomUUID()));
  }

  @Test
  @DisplayName("Deve validar prioridade de execução com sucesso quando é a próxima")
  void deveValidarPrioridadeExecucaoQuandoProxima() {
    UUID id = UUID.randomUUID();
    OrdemServico os = new OrdemServico();
    os.setId(id);

    when(repository.listarFilaExecucao(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(os)));

    assertDoesNotThrow(() -> service.validarPrioridadeExecucao(id));
  }

  @Test
  @DisplayName("Deve lançar exceção quando validar prioridade de execução e não for a próxima")
  void deveLancarExcecaoValidarPrioridadeExecucao() {
    UUID idSolicitado = UUID.randomUUID();
    UUID idEsperado = UUID.randomUUID();

    OrdemServico osEsperada = new OrdemServico();
    osEsperada.setId(idEsperado);
    osEsperada.setCodigo("OS-456");
    osEsperada.setPrioridade(Prioridade.URGENTE);

    when(repository.listarFilaExecucao(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(osEsperada)));

    ViolacaoPrioridadeException ex =
        assertThrows(
            ViolacaoPrioridadeException.class,
            () -> service.validarPrioridadeExecucao(idSolicitado));

    assertEquals(
        "Violação de prioridade na fila de execução. A OS %s deveria ser processada antes da %s."
            .formatted("OS-456", idSolicitado),
        ex.getMessage());
  }

  @Test
  @DisplayName("Deve atualizar prioridade da OS via PrioridadeService")
  void deveAtualizarPrioridade() {
    UUID id = UUID.randomUUID();
    OrdemServico os = new OrdemServico();
    os.setId(id);
    os.setPrioridade(Prioridade.BAIXA);

    when(repository.findById(id)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));

    OrdemServico result = service.atualizarPrioridade(id, Prioridade.URGENTE);

    assertEquals(Prioridade.URGENTE, result.getPrioridade());
    verify(repository).save(os);
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar prioridade de OS inexistente")
  void deveLancarExcecaoAoAtualizarPrioridadeInexistente() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        OrdemServicoNaoEncontradaException.class,
        () -> service.atualizarPrioridade(id, Prioridade.ALTA));
  }
}
