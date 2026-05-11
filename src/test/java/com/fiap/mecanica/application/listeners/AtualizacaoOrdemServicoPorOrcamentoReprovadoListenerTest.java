package com.fiap.mecanica.application.listeners;

import static org.mockito.Mockito.verify;

import com.fiap.mecanica.application.events.OrcamentoReprovadoEvent;
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
class AtualizacaoOrdemServicoPorOrcamentoReprovadoListenerTest {

  @Mock private OsLifecycleService lifecycleService;

  @InjectMocks private AtualizacaoOrdemServicoPorOrcamentoReprovadoListener listener;

  @Test
  @DisplayName("Deve cancelar OS ao receber OrcamentoReprovadoEvent")
  void deveCancelarOsQuandoOrcamentoReprovado() {
    UUID osId = UUID.randomUUID();
    UUID orcamentoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);

    OrcamentoReprovadoEvent event = new OrcamentoReprovadoEvent(this, orcamento);

    listener.handleOrcamentoReprovado(event);

    verify(lifecycleService).cancelar(osId);
  }
}
