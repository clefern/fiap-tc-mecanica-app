package com.fiap.mecanica.application.listeners;

import com.fiap.mecanica.application.events.OrdemServicoAguardandoAprovacaoEvent;
import com.fiap.mecanica.application.service.OrcamentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeracaoOrcamentoListener {

  private final OrcamentoService orcamentoService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleOrdemServicoAguardandoAprovacao(OrdemServicoAguardandoAprovacaoEvent event) {
    log.info(
        "Iniciando geração automática de orçamento para OS: {}",
        event.getOrdemServico().getCodigo());
    try {
      if (orcamentoService.buscarPorOrdemServico(event.getOrdemServico().getId()).isPresent()) {
        log.info(
            "Orçamento já existe para OS: {}. Pulando geração automática.",
            event.getOrdemServico().getCodigo());
        return;
      }
      orcamentoService.gerarOrcamento(event.getOrdemServico());
      log.info("Orçamento gerado com sucesso para OS: {}", event.getOrdemServico().getCodigo());
    } catch (Exception e) {
      log.error("❌ Erro ao gerar orçamento para OS: {}", event.getOrdemServico().getCodigo(), e);
    }
  }
}
