package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.events.OrdemServicoAguardandoAprovacaoEvent;
import com.fiap.mecanica.application.events.OrdemServicoCanceladaEvent;
import com.fiap.mecanica.application.events.OsFinalizadaEvent;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.application.service.os.OsMecanicoAssigner;
import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OsLifecycleServiceImplTest {

  @Mock private OrdemServicoRepository repository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private OrcamentoService orcamentoService;
  @Mock private PrioridadeService prioridadeService;
  @Mock private OsMecanicoAssigner mecanicoAssigner;

  @InjectMocks private OsLifecycleServiceImpl service;

  private UUID osId;
  private UUID mecanicoId;
  private OrdemServico os;

  @BeforeEach
  void setUp() {
    osId = UUID.randomUUID();
    mecanicoId = UUID.randomUUID();
    os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(osId);
  }

  private void addItem() {
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .tipo(TipoItem.PECA)
            .descricao("Item")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .build();
    os.adicionarItem(item);
  }

  @Test
  @DisplayName("Deve iniciar diagnóstico com validação de prioridade")
  void deveIniciarDiagnosticoComValidacaoDePrioridade() {
    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.iniciarDiagnostico(osId, mecanicoId);

    assertThat(result.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
    verify(prioridadeService).validarPrioridadeOrcamento(osId);
    verify(mecanicoAssigner).assign(os, mecanicoId);
  }

  @Test
  @DisplayName("Deve iniciar diagnóstico sem validar prioridade quando retornando de aprovação")
  void deveIniciarDiagnosticoSemPrioridadeParaRetornoDeAprovacao() {
    OrdemServico mockOs = mock(OrdemServico.class);
    when(mockOs.getStatus()).thenReturn(StatusOS.AGUARDANDO_APROVACAO);
    when(mockOs.getId()).thenReturn(osId);
    when(repository.findById(osId)).thenReturn(Optional.of(mockOs));
    when(repository.save(mockOs)).thenReturn(mockOs);

    service.iniciarDiagnostico(osId, mecanicoId);

    verify(prioridadeService, never()).validarPrioridadeOrcamento(osId);
    verify(orcamentoService).cancelarOrcamentosPendentes(osId);
    verify(mockOs).iniciarDiagnostico();
  }

  @Test
  @DisplayName("Deve trocar mecânico responsável")
  void deveTrocarMecanicoResponsavel() {
    UUID novoMecanicoId = UUID.randomUUID();
    os.atribuirMecanicoDiagnostico(mecanicoId);
    os.iniciarDiagnostico();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.trocarMecanicoResponsavel(osId, novoMecanicoId);

    assertThat(result.getMecanicoDiagnosticoId()).isEqualTo(novoMecanicoId);
  }

  @Test
  @DisplayName("Deve finalizar diagnóstico e publicar evento de aguardando aprovação")
  void deveFinalizarDiagnosticoEPublicarEvento() {
    addItem();
    os.atribuirMecanicoDiagnostico(mecanicoId);
    os.iniciarDiagnostico();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.finalizarDiagnostico(osId, mecanicoId);

    assertThat(result.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
    verify(eventPublisher).publishEvent(any(OrdemServicoAguardandoAprovacaoEvent.class));
    verify(mecanicoAssigner).assign(os, mecanicoId);
  }

  @Test
  @DisplayName("Deve iniciar execução com validação de prioridade")
  void deveIniciarExecucaoComValidacaoDePrioridade() {
    addItem();
    os.atribuirMecanicoDiagnostico(mecanicoId);
    os.iniciarDiagnostico();
    os.emitirOrcamento();
    os.aprovar();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.iniciarExecucao(osId, mecanicoId);

    assertThat(result.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
    assertThat(result.getMecanicoExecucaoId()).isEqualTo(mecanicoId);
    verify(prioridadeService).validarPrioridadeExecucao(osId);
  }

  @Test
  @DisplayName("Não deve reatribuir mecânico de execução se já definido")
  void naoDeveReatribuirMecanicoExecucaoSeJaDefinido() {
    UUID mecanicoExecucaoExistente = UUID.randomUUID();
    OrdemServico mockOs = mock(OrdemServico.class);
    when(mockOs.getMecanicoExecucaoId()).thenReturn(mecanicoExecucaoExistente);
    when(repository.findById(osId)).thenReturn(Optional.of(mockOs));
    when(repository.save(mockOs)).thenReturn(mockOs);

    service.iniciarExecucao(osId, mecanicoId);

    verify(mockOs, never()).atribuirMecanicoExecucao(any());
    verify(mockOs).iniciarExecucao();
  }

  @Test
  @DisplayName("Deve finalizar OS e publicar evento")
  void deveFinalizarOsEPublicarEvento() {
    addItem();
    os.atribuirMecanicoDiagnostico(mecanicoId);
    os.iniciarDiagnostico();
    os.emitirOrcamento();
    os.aprovar();
    os.atribuirMecanicoExecucao(mecanicoId);
    os.iniciarExecucao();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.finalizar(osId, mecanicoId);

    assertThat(result.getStatus()).isEqualTo(StatusOS.FINALIZADA);
    verify(eventPublisher).publishEvent(any(OsFinalizadaEvent.class));
    verify(mecanicoAssigner).assign(os, mecanicoId);
  }

  @Test
  @DisplayName("Deve aprovar OS")
  void deveAprovarOS() {
    addItem();
    os.atribuirMecanicoDiagnostico(mecanicoId);
    os.iniciarDiagnostico();
    os.emitirOrcamento();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.aprovarOS(osId);

    assertThat(result.getStatus()).isEqualTo(StatusOS.APROVADA);
  }

  @Test
  @DisplayName("Deve entregar OS")
  void deveEntregarOS() {
    addItem();
    os.atribuirMecanicoDiagnostico(mecanicoId);
    os.iniciarDiagnostico();
    os.emitirOrcamento();
    os.aprovar();
    os.atribuirMecanicoExecucao(mecanicoId);
    os.iniciarExecucao();
    os.finalizar();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.entregar(osId);

    assertThat(result.getStatus()).isEqualTo(StatusOS.ENTREGUE);
  }

  @Test
  @DisplayName("Deve cancelar OS e publicar evento")
  void deveCancelarOsEPublicarEvento() {
    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.cancelar(osId);

    assertThat(result.getStatus()).isEqualTo(StatusOS.CANCELADA);
    verify(eventPublisher).publishEvent(any(OrdemServicoCanceladaEvent.class));
  }
}
