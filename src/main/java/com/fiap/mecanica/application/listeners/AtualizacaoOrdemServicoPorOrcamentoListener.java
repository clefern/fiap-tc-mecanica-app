package com.fiap.mecanica.application.listeners;

import com.fiap.mecanica.application.events.OrcamentoAprovadoEvent;
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
public class AtualizacaoOrdemServicoPorOrcamentoListener {

  private final OsLifecycleService lifecycleService;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleOrcamentoAprovado(OrcamentoAprovadoEvent event) {
    UUID osId = event.getOrcamento().getOrdemServicoId();
    log.info(
        "Atualizando OS {} para APROVADA a partir da aprovação do orçamento {}",
        osId,
        event.getOrcamento().getId());
    lifecycleService.aprovarOS(osId);
  }
}
