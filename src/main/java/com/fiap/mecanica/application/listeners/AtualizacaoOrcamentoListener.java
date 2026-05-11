package com.fiap.mecanica.application.listeners;

import com.fiap.mecanica.application.events.OrdemServicoCanceladaEvent;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.model.Orcamento;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AtualizacaoOrcamentoListener {

  private final OrcamentoService orcamentoService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrdemServicoCancelada(OrdemServicoCanceladaEvent event) {
    log.info("Atualizando orçamento para CANCELADO da OS: {}", event.getOrdemServico().getCodigo());
    try {
      Optional<Orcamento> orcamentoOpt =
          orcamentoService.buscarPorOrdemServico(event.getOrdemServico().getId());
      if (orcamentoOpt.isPresent() && orcamentoOpt.get().getStatus() == StatusOrcamento.GERADO) {
        orcamentoService.cancelar(orcamentoOpt.get().getId());
        log.info("Orçamento atualizado com sucesso para CANCELADO.");
      }
    } catch (Exception e) {
      log.error(
          "❌ Erro ao atualizar orçamento para OS: {}", event.getOrdemServico().getCodigo(), e);
    }
  }
}
