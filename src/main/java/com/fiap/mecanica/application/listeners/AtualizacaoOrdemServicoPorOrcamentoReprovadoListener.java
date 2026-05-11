package com.fiap.mecanica.application.listeners;

import com.fiap.mecanica.application.events.OrcamentoReprovadoEvent;
import com.fiap.mecanica.application.service.OsLifecycleService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AtualizacaoOrdemServicoPorOrcamentoReprovadoListener {

  private final OsLifecycleService lifecycleService;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleOrcamentoReprovado(OrcamentoReprovadoEvent event) {
    UUID osId = event.getOrcamento().getOrdemServicoId();
    log.info(
        "Atualizando OS {} para CANCELADA a partir da reprovação do orçamento {}",
        osId,
        event.getOrcamento().getId());
    lifecycleService.cancelar(osId);
  }
}
