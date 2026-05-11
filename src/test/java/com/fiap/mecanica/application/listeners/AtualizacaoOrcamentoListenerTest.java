package com.fiap.mecanica.application.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.events.OrdemServicoCanceladaEvent;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AtualizacaoOrcamentoListenerTest {

  @Mock private OrcamentoService orcamentoService;

  @InjectMocks private AtualizacaoOrcamentoListener listener;

  @Test
  @DisplayName("Deve cancelar orçamento GERADO quando OS for cancelada")
  void deveCancelarOrcamentoGeradoQuandoOsCancelada() {
    UUID osId = UUID.randomUUID();

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setCodigo("OS-123");

    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setStatus(StatusOrcamento.GERADO);

    when(orcamentoService.buscarPorOrdemServico(osId)).thenReturn(Optional.of(orcamento));

    OrdemServicoCanceladaEvent event = new OrdemServicoCanceladaEvent(this, os);

    listener.handleOrdemServicoCancelada(event);

    verify(orcamentoService).cancelar(orcamento.getId());
  }

  @Test
  @DisplayName("Não deve cancelar orçamento se status não for GERADO")
  void naoDeveCancelarOrcamentoSeStatusNaoForGerado() {
    UUID osId = UUID.randomUUID();

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setCodigo("OS-456");

    Orcamento orcamentoRejeitado = new Orcamento();
    orcamentoRejeitado.setId(UUID.randomUUID());
    orcamentoRejeitado.setStatus(StatusOrcamento.REJEITADO);

    when(orcamentoService.buscarPorOrdemServico(osId)).thenReturn(Optional.of(orcamentoRejeitado));

    OrdemServicoCanceladaEvent event = new OrdemServicoCanceladaEvent(this, os);

    listener.handleOrdemServicoCancelada(event);

    verify(orcamentoService, never()).cancelar(orcamentoRejeitado.getId());
  }

  @Test
  @DisplayName("Não deve fazer nada se orçamento não encontrado")
  void naoDeveFazerNadaSeOrcamentoNaoEncontrado() {
    UUID osId = UUID.randomUUID();
    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setCodigo("OS-999");

    when(orcamentoService.buscarPorOrdemServico(osId)).thenReturn(Optional.empty());

    OrdemServicoCanceladaEvent event = new OrdemServicoCanceladaEvent(this, os);

    listener.handleOrdemServicoCancelada(event);

    verify(orcamentoService, never()).cancelar(any());
  }

  @Test
  @DisplayName("Deve logar erro mas não falhar quando exceção ocorrer")
  void deveLogarErroQuandoExcecaoOcorrer() {
    UUID osId = UUID.randomUUID();
    OrdemServico os = new OrdemServico();
    os.setId(osId);

    when(orcamentoService.buscarPorOrdemServico(osId)).thenThrow(new RuntimeException("DB Error"));

    OrdemServicoCanceladaEvent event = new OrdemServicoCanceladaEvent(this, os);

    // Should not throw exception
    listener.handleOrdemServicoCancelada(event);
  }
}
