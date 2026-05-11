package com.fiap.mecanica.application.service.prioridade.impl;

import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.domain.exception.ViolacaoPrioridadeException;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PrioridadeServiceImpl implements PrioridadeService {

  private final OrdemServicoRepository repository;

  public PrioridadeServiceImpl(OrdemServicoRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrdemServico> listarFilaOrcamento(Pageable pageable) {
    return repository.listarFilaOrcamento(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrdemServico> listarFilaExecucao(Pageable pageable) {
    return repository.listarFilaExecucao(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OrdemServico> obterProximaParaOrcamento() {
    Page<OrdemServico> page = repository.listarFilaOrcamento(PageRequest.of(0, 1));
    return page.stream().findFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OrdemServico> obterProximaParaExecucao() {
    Page<OrdemServico> page = repository.listarFilaExecucao(PageRequest.of(0, 1));
    return page.stream().findFirst();
  }

  @Override
  @Transactional(readOnly = true)
  public void validarPrioridadeOrcamento(UUID osId) {
    Optional<OrdemServico> proxima = obterProximaParaOrcamento();

    if (proxima.isPresent() && !proxima.get().getId().equals(osId)) {
      OrdemServico expected = proxima.get();
      logErroPrioridade("ORCAMENTO", osId, expected);
      throw new ViolacaoPrioridadeException(
          String.format(
              "Violação de prioridade na fila de orçamento. A OS %s deveria ser processada antes da"
                  + " %s.",
              expected.getCodigo(), osId));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void validarPrioridadeExecucao(UUID osId) {
    Optional<OrdemServico> proxima = obterProximaParaExecucao();

    if (proxima.isPresent() && !proxima.get().getId().equals(osId)) {
      OrdemServico expected = proxima.get();
      logErroPrioridade("EXECUCAO", osId, expected);
      throw new ViolacaoPrioridadeException(
          String.format(
              "Violação de prioridade na fila de execução. A OS %s deveria ser processada antes da"
                  + " %s.",
              expected.getCodigo(), osId));
    }
  }

  @Override
  @Transactional
  public OrdemServico atualizarPrioridade(UUID id, Prioridade novaPrioridade) {
    OrdemServico os =
        repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
    Prioridade prioridadeAnterior = os.getPrioridade();
    os.setPrioridade(novaPrioridade);
    log.info(
        "[OS_PRIORIDADE_ATUALIZADA] OS={} PrioridadeAnterior={} NovaPrioridade={}",
        os.getId(),
        prioridadeAnterior,
        novaPrioridade);
    return repository.save(os);
  }

  private void logErroPrioridade(String fila, UUID osTentada, OrdemServico osEsperada) {
    log.error(
        "❌ [VIOLACAO_PRIORIDADE] Fila: {} | Timestamp: {} | OS Tentada: {} | OS Esperada: {}"
            + " (Prioridade: {})",
        fila,
        LocalDateTime.now(),
        osTentada,
        osEsperada.getId(),
        osEsperada.getPrioridade());
  }
}
