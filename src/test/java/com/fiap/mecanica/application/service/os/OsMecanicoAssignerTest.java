package com.fiap.mecanica.application.service.os;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OsMecanicoAssignerTest {

  private OsMecanicoAssigner assigner;

  @BeforeEach
  void setUp() {
    assigner = new OsMecanicoAssigner();
  }

  @Test
  @DisplayName("Deve ser no-op quando mecanicoId é nulo")
  void deveSerNoOpQuandoMecanicoIdNulo() {
    OrdemServico os = mock(OrdemServico.class);

    assigner.assign(os, null);

    verify(os, never()).atribuirMecanicoDiagnostico(org.mockito.ArgumentMatchers.any());
    verify(os, never()).atribuirMecanicoExecucao(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("Deve atribuir mecânico de diagnóstico quando status é RECEBIDA")
  void deveAtribuirMecanicoDiagnosticoParaStatusRecebida() {
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    OrdemServico os = OrdemServico.nova(clienteId, veiculoId);
    UUID mecanicoId = UUID.randomUUID();

    assigner.assign(os, mecanicoId);

    assertThat(os.getMecanicoDiagnosticoId()).isEqualTo(mecanicoId);
  }

  @Test
  @DisplayName("Deve atribuir mecânico de diagnóstico quando status é EM_DIAGNOSTICO")
  void deveAtribuirMecanicoDiagnosticoParaStatusEmDiagnostico() {
    OrdemServico os = mock(OrdemServico.class);
    when(os.getStatus()).thenReturn(StatusOS.EM_DIAGNOSTICO);
    when(os.getMecanicoDiagnosticoId()).thenReturn(null);
    UUID mecanicoId = UUID.randomUUID();

    assigner.assign(os, mecanicoId);

    verify(os).atribuirMecanicoDiagnostico(mecanicoId);
  }

  @Test
  @DisplayName("Deve atribuir mecânico de diagnóstico quando status é AGUARDANDO_APROVACAO")
  void deveAtribuirMecanicoDiagnosticoParaStatusAguardandoAprovacao() {
    OrdemServico os = mock(OrdemServico.class);
    when(os.getStatus()).thenReturn(StatusOS.AGUARDANDO_APROVACAO);
    when(os.getMecanicoDiagnosticoId()).thenReturn(null);
    UUID mecanicoId = UUID.randomUUID();

    assigner.assign(os, mecanicoId);

    verify(os).atribuirMecanicoDiagnostico(mecanicoId);
  }

  @Test
  @DisplayName("Não deve reatribuir mecânico de diagnóstico se já atribuído")
  void naoDeveReatribuirMecanicoDiagnosticoSeJaAtribuido() {
    OrdemServico os = mock(OrdemServico.class);
    when(os.getStatus()).thenReturn(StatusOS.RECEBIDA);
    UUID existente = UUID.randomUUID();
    when(os.getMecanicoDiagnosticoId()).thenReturn(existente);

    assigner.assign(os, UUID.randomUUID());

    verify(os, never()).atribuirMecanicoDiagnostico(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("Deve atribuir mecânico de execução para status de execução")
  void deveAtribuirMecanicoExecucaoParaStatusExecucao() {
    OrdemServico os = mock(OrdemServico.class);
    when(os.getStatus()).thenReturn(StatusOS.EM_EXECUCAO);
    when(os.getMecanicoExecucaoId()).thenReturn(null);
    UUID mecanicoId = UUID.randomUUID();

    assigner.assign(os, mecanicoId);

    verify(os).atribuirMecanicoExecucao(mecanicoId);
  }

  @Test
  @DisplayName("Não deve reatribuir mecânico de execução se já atribuído")
  void naoDeveReatribuirMecanicoExecucaoSeJaAtribuido() {
    OrdemServico os = mock(OrdemServico.class);
    when(os.getStatus()).thenReturn(StatusOS.EM_EXECUCAO);
    UUID existente = UUID.randomUUID();
    when(os.getMecanicoExecucaoId()).thenReturn(existente);

    assigner.assign(os, UUID.randomUUID());

    verify(os, never()).atribuirMecanicoExecucao(org.mockito.ArgumentMatchers.any());
  }
}
