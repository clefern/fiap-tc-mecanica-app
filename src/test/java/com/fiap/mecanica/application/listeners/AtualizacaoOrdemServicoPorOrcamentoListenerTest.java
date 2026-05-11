package com.fiap.mecanica.application.listeners;

import static org.mockito.Mockito.verify;

import com.fiap.mecanica.application.events.OrcamentoAprovadoEvent;
import com.fiap.mecanica.application.service.OsLifecycleService;
import com.fiap.mecanica.domain.model.Orcamento;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AtualizacaoOrdemServicoPorOrcamentoListenerTest {

  @Mock private OsLifecycleService lifecycleService;

  @InjectMocks private AtualizacaoOrdemServicoPorOrcamentoListener listener;

  @Test
  @DisplayName("Deve aprovar OS ao receber OrcamentoAprovadoEvent")
  void deveAprovarOsQuandoOrcamentoAprovado() {
    UUID osId = UUID.randomUUID();
    UUID orcamentoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);

    OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent(this, orcamento);

    listener.handleOrcamentoAprovado(event);

    verify(lifecycleService).aprovarOS(osId);
  }
}
