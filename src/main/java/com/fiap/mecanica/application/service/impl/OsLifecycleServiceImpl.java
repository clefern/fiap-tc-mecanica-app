package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.events.*;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.application.service.OsLifecycleService;
import com.fiap.mecanica.application.service.os.OsMecanicoAssigner;
import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OsLifecycleServiceImpl implements OsLifecycleService {

  private final OrdemServicoRepository repository;
  private final ApplicationEventPublisher eventPublisher;
  private final OrcamentoService orcamentoService;
  private final PrioridadeService prioridadeService;
  private final OsMecanicoAssigner mecanicoAssigner;

  public OsLifecycleServiceImpl(
      OrdemServicoRepository repository,
      ApplicationEventPublisher eventPublisher,
      OrcamentoService orcamentoService,
      PrioridadeService prioridadeService,
      OsMecanicoAssigner mecanicoAssigner) {
    this.repository = repository;
    this.eventPublisher = eventPublisher;
    this.orcamentoService = orcamentoService;
    this.prioridadeService = prioridadeService;
    this.mecanicoAssigner = mecanicoAssigner;
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.OS_DIAGNOSIS_STARTED)
  public OrdemServico iniciarDiagnostico(UUID id, UUID mecanicoId) {
    log.info("[OS_INICIAR_DIAGNOSTICO] ID={} Mecanico={}", id, mecanicoId);
    OrdemServico os = findOrThrow(id);
    boolean isRetornoDeAprovacao = os.getStatus() == StatusOS.AGUARDANDO_APROVACAO;

    if (!isRetornoDeAprovacao) {
      prioridadeService.validarPrioridadeOrcamento(id);
    }

    mecanicoAssigner.assign(os, mecanicoId);
    os.iniciarDiagnostico();
    OrdemServico saved = repository.save(os);
    eventPublisher.publishEvent(new OrdemServicoEmDiagnosticoEvent(this, saved));

    if (isRetornoDeAprovacao) {
      orcamentoService.cancelarOrcamentosPendentes(saved.getId());
    }

    return saved;
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.OS_MECHANIC_CHANGED)
  public OrdemServico trocarMecanicoResponsavel(UUID id, UUID novoMecanicoId) {
    log.info("[OS_TROCAR_MECANICO] ID={} NovoMecanico={}", id, novoMecanicoId);
    OrdemServico os = findOrThrow(id);
    os.trocarMecanicoResponsavel(novoMecanicoId);
    return repository.save(os);
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.OS_DIAGNOSIS_COMPLETED)
  public OrdemServico finalizarDiagnostico(UUID id, UUID mecanicoId) {
    log.info("[OS_FINALIZAR_DIAGNOSTICO] ID={} Mecanico={}", id, mecanicoId);
    OrdemServico os = findOrThrow(id);
    mecanicoAssigner.assign(os, mecanicoId);
    os.emitirOrcamento();
    OrdemServico saved = repository.save(os);
    eventPublisher.publishEvent(new OrdemServicoAguardandoAprovacaoEvent(this, saved));
    return saved;
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.OS_STARTED)
  @MonitoredOperation(type = MonitoredOperationType.MECHANIC_PERFORMANCE)
  public OrdemServico iniciarExecucao(UUID id, UUID mecanicoId) {
    log.info("[OS_INICIAR_EXECUCAO] ID={} Mecanico={}", id, mecanicoId);
    OrdemServico os = findOrThrow(id);
    prioridadeService.validarPrioridadeExecucao(id);

    if (os.getMecanicoExecucaoId() == null) {
      os.atribuirMecanicoExecucao(mecanicoId);
    }

    os.iniciarExecucao();
    OrdemServico saved = repository.save(os);
    eventPublisher.publishEvent(new OrdemServicoEmExecucaoEvent(this, saved));
    return saved;
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.OS_COMPLETED)
  @MonitoredOperation(type = MonitoredOperationType.MECHANIC_PERFORMANCE)
  public OrdemServico finalizar(UUID id, UUID mecanicoId) {
    log.info("[OS_FINALIZAR] ID={} Mecanico={}", id, mecanicoId);
    OrdemServico os = findOrThrow(id);
    mecanicoAssigner.assign(os, mecanicoId);
    os.finalizar();
    OrdemServico saved = repository.save(os);
    eventPublisher.publishEvent(new OsFinalizadaEvent(this, saved));
    return saved;
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.OS_APPROVED)
  public OrdemServico aprovarOS(UUID id) {
    log.info("[OS_APROVAR] ID={}", id);
    OrdemServico os = findOrThrow(id);
    os.aprovar();
    OrdemServico saved = repository.save(os);
    eventPublisher.publishEvent(new OrdemServicoAprovadaEvent(this, saved));
    return saved;
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.OS_DELIVERED)
  public OrdemServico entregar(UUID id) {
    log.info("[OS_ENTREGAR] ID={}", id);
    OrdemServico os = findOrThrow(id);
    os.entregar();
    OrdemServico saved = repository.save(os);
    eventPublisher.publishEvent(new OrdemServicoEntregueEvent(this, saved));
    return saved;
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.OS_CANCELLED)
  public OrdemServico cancelar(UUID id) {
    log.info("[OS_CANCELAR] ID={}", id);
    OrdemServico os = findOrThrow(id);
    os.cancelar();
    OrdemServico saved = repository.save(os);
    eventPublisher.publishEvent(new OrdemServicoCanceladaEvent(this, saved));
    return saved;
  }

  private OrdemServico findOrThrow(UUID id) {
    return repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
  }
}
