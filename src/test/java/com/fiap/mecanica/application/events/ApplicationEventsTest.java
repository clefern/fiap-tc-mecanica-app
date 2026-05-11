package com.fiap.mecanica.application.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApplicationEventsTest {

  @Test
  @DisplayName("Should create all event types correctly")
  void shouldCreateAllEventTypesCorrectly() {
    Object source = new Object();
    OrdemServico os = mock(OrdemServico.class);
    Orcamento orcamento = mock(Orcamento.class);

    // OsCriadaEvent
    OsCriadaEvent osCriada = new OsCriadaEvent(source, os);
    assertThat(osCriada.getOrdemServico()).isEqualTo(os);
    assertThat(osCriada.getSource()).isEqualTo(source);

    // OrcamentoGeradoEvent
    OrcamentoGeradoEvent orcamentoGerado = new OrcamentoGeradoEvent(source, orcamento);
    assertThat(orcamentoGerado.getOrcamento()).isEqualTo(orcamento);

    // OrcamentoAprovadoEvent
    OrcamentoAprovadoEvent orcamentoAprovado = new OrcamentoAprovadoEvent(source, orcamento);
    assertThat(orcamentoAprovado.getOrcamento()).isEqualTo(orcamento);

    // OrcamentoReprovadoEvent
    OrcamentoReprovadoEvent orcamentoReprovado = new OrcamentoReprovadoEvent(source, orcamento);
    assertThat(orcamentoReprovado.getOrcamento()).isEqualTo(orcamento);

    // OrdemServicoAprovadaEvent
    OrdemServicoAprovadaEvent osAprovada = new OrdemServicoAprovadaEvent(source, os);
    assertThat(osAprovada.getOrdemServico()).isEqualTo(os);

    // OrdemServicoAguardandoAprovacaoEvent
    OrdemServicoAguardandoAprovacaoEvent osAguardando =
        new OrdemServicoAguardandoAprovacaoEvent(source, os);
    assertThat(osAguardando.getOrdemServico()).isEqualTo(os);

    // OrdemServicoCanceladaEvent
    OrdemServicoCanceladaEvent osCancelada = new OrdemServicoCanceladaEvent(source, os);
    assertThat(osCancelada.getOrdemServico()).isEqualTo(os);

    // OsFinalizadaEvent
    OsFinalizadaEvent osFinalizada = new OsFinalizadaEvent(source, os);
    assertThat(osFinalizada.getOrdemServico()).isEqualTo(os);
  }
}
