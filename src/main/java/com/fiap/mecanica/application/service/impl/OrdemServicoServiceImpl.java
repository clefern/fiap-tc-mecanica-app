package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.events.OsCriadaEvent;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.application.service.OrdemServicoService;
import com.fiap.mecanica.application.service.os.OsEntityValidator;
import com.fiap.mecanica.application.service.os.OsEstoqueValidator;
import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OrdemServicoServiceImpl implements OrdemServicoService {

  private final OrdemServicoRepository repository;
  private final ApplicationEventPublisher eventPublisher;
  private final OrcamentoService orcamentoService;
  private final PrioridadeService prioridadeService;
  private final OsEntityValidator entityValidator;
  private final OsEstoqueValidator estoqueValidator;

  public OrdemServicoServiceImpl(
      OrdemServicoRepository repository,
      ApplicationEventPublisher eventPublisher,
      OrcamentoService orcamentoService,
      PrioridadeService prioridadeService,
      OsEntityValidator entityValidator,
      OsEstoqueValidator estoqueValidator) {
    this.repository = repository;
    this.eventPublisher = eventPublisher;
    this.orcamentoService = orcamentoService;
    this.prioridadeService = prioridadeService;
    this.entityValidator = entityValidator;
    this.estoqueValidator = estoqueValidator;
  }

  @Override
  @Transactional
  public OrdemServico criarOrdemServico(UUID clienteId, UUID veiculoId, String observacoes) {
    entityValidator.validar(clienteId, veiculoId);

    OrdemServico os = OrdemServico.nova(clienteId, veiculoId);
    os.setObservacoes(observacoes);

    OrdemServico saved = repository.save(os);
    log.info("[OS_CRIADA] ID={} Cliente={} Veiculo={}", saved.getId(), clienteId, veiculoId);
    eventPublisher.publishEvent(new OsCriadaEvent(this, saved));

    return saved;
  }

  @Override
  @Transactional
  public OrdemServico abrirOsCompleta(
      UUID clienteId, UUID veiculoId, String observacoes, List<ItemOrdemServico> itens) {
    entityValidator.validar(clienteId, veiculoId);

    OrdemServico os = OrdemServico.nova(clienteId, veiculoId);
    os.setObservacoes(observacoes);

    if (itens != null) {
      itens.forEach(
          item -> {
            estoqueValidator.validar(item);
            os.adicionarItem(item);
          });
    }

    OrdemServico saved = repository.save(os);
    log.info(
        "[OS_ABERTURA_COMPLETA] ID={} Cliente={} Veiculo={} Itens={}",
        saved.getId(),
        clienteId,
        veiculoId,
        saved.getItens().size());
    eventPublisher.publishEvent(new OsCriadaEvent(this, saved));

    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public OrdemServico buscarPorId(UUID id) {
    return repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public OrdemServico buscarPorCodigo(String codigo) {
    return repository
        .findByCodigo(codigo)
        .orElseThrow(() -> new OrdemServicoNaoEncontradaException(codigo));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrdemServico> listarTodas(StatusOS status, UUID clienteId, Pageable pageable) {
    if (status == null && clienteId == null) {
      return repository.findAll(pageable);
    }
    return repository.findByFilters(status, clienteId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrdemServico> listarFilaOperacional(Pageable pageable) {
    log.debug("Listando fila operacional de OS");
    return repository.listarFilaOperacional(pageable);
  }
}
