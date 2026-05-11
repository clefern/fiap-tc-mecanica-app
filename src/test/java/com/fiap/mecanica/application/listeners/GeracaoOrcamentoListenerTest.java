package com.fiap.mecanica.application.listeners;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.events.OrdemServicoAguardandoAprovacaoEvent;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.domain.model.OrdemServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeracaoOrcamentoListenerTest {

  @Mock private OrcamentoService orcamentoService;

  @InjectMocks private GeracaoOrcamentoListener listener;

  @Test
  @DisplayName("Deve gerar orçamento quando evento recebido")
  void deveGerarOrcamento() {
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-123");
    OrdemServicoAguardandoAprovacaoEvent event = new OrdemServicoAguardandoAprovacaoEvent(this, os);

    listener.handleOrdemServicoAguardandoAprovacao(event);

    verify(orcamentoService, times(1)).gerarOrcamento(os);
  }

  @Test
  @DisplayName("Não deve gerar orçamento se já existir")
  void naoDeveGerarOrcamentoSeJaExistir() {
    OrdemServico os = new OrdemServico();
    os.setId(java.util.UUID.randomUUID());
    os.setCodigo("OS-123");
    OrdemServicoAguardandoAprovacaoEvent event = new OrdemServicoAguardandoAprovacaoEvent(this, os);

    when(orcamentoService.buscarPorOrdemServico(os.getId()))
        .thenReturn(java.util.Optional.of(new com.fiap.mecanica.domain.model.Orcamento()));

    listener.handleOrdemServicoAguardandoAprovacao(event);

    verify(orcamentoService, times(0)).gerarOrcamento(os);
  }

  @Test
  @DisplayName("Deve logar erro quando falhar ao gerar orçamento")
  void deveLogarErroQuandoFalhar() {
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-123");
    OrdemServicoAguardandoAprovacaoEvent event = new OrdemServicoAguardandoAprovacaoEvent(this, os);

    doThrow(new RuntimeException("Erro")).when(orcamentoService).gerarOrcamento(os);

    listener.handleOrdemServicoAguardandoAprovacao(event);

    verify(orcamentoService, times(1)).gerarOrcamento(os);
  }
}
