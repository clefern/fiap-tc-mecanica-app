package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.infra.entity.ItemOrdemServicoEntity;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import com.fiap.mecanica.infra.mapper.OrdemServicoEntityMapper;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class JpaOrdemServicoRepositoryAdapter implements OrdemServicoRepository {

  private final JpaOrdemServicoRepository jpaRepository;
  private final OrdemServicoEntityMapper mapper;

  public JpaOrdemServicoRepositoryAdapter(
      JpaOrdemServicoRepository jpaRepository, OrdemServicoEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  @MonitoredOperation(type = MonitoredOperationType.APPLICATION_PERFORMANCE_DB_QUERY)
  public OrdemServico save(OrdemServico ordemServico) {
    if (ordemServico.getId() == null) {
      return insertNew(ordemServico);
    }
    return jpaRepository
        .findWithItensById(ordemServico.getId())
        .map(managed -> updateManaged(managed, ordemServico))
        .orElseGet(() -> insertNew(ordemServico));
  }

  private OrdemServico insertNew(OrdemServico ordemServico) {
    OrdemServicoEntity entity = mapper.toEntity(ordemServico);
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(LocalDateTime.now());
    }
    entity.setUpdatedAt(LocalDateTime.now());
    OrdemServicoEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  private OrdemServico updateManaged(OrdemServicoEntity managed, OrdemServico domain) {
    mapper.updateEntity(managed, domain);
    managed.setUpdatedAt(LocalDateTime.now());
    reconcileItens(managed, domain);
    return mapper.toDomain(managed);
  }

  private void reconcileItens(OrdemServicoEntity managed, OrdemServico domain) {
    if (managed.getItens() == null) {
      managed.setItens(new ArrayList<>());
    }
    Map<UUID, ItemOrdemServicoEntity> existentes = new HashMap<>();
    for (ItemOrdemServicoEntity itemManaged : managed.getItens()) {
      if (itemManaged.getId() != null) {
        existentes.put(itemManaged.getId(), itemManaged);
      }
    }

    Set<UUID> idsDoDomain = new HashSet<>();
    if (domain.getItens() != null) {
      for (ItemOrdemServico itemDom : domain.getItens()) {
        UUID idDom = itemDom.getId();
        if (idDom != null && existentes.containsKey(idDom)) {
          mapper.updateItem(existentes.get(idDom), itemDom);
          idsDoDomain.add(idDom);
        } else {
          ItemOrdemServicoEntity novo = mapper.toEntityItem(itemDom);
					novo.setId(null);
          novo.setOrdemServico(managed);
          managed.getItens().add(novo);
          if (idDom != null) {
            idsDoDomain.add(idDom);
          }
        }
      }
    }

    managed.getItens().removeIf(i -> i.getId() != null && !idsDoDomain.contains(i.getId()));
  }

  @Override
  @MonitoredOperation(type = MonitoredOperationType.APPLICATION_PERFORMANCE_DB_QUERY)
  public Optional<OrdemServico> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  @MonitoredOperation(type = MonitoredOperationType.APPLICATION_PERFORMANCE_DB_QUERY)
  public Optional<OrdemServico> findByIdWithItens(UUID id) {
    return jpaRepository.findWithItensById(id).map(mapper::toDomain);
  }

  @Override
  @MonitoredOperation(type = MonitoredOperationType.APPLICATION_PERFORMANCE_DB_QUERY)
  public Page<OrdemServico> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public Page<OrdemServico> listarFilaOrcamento(Pageable pageable) {
    return jpaRepository
        .findByStatusOrderByPrioridadeDescCreatedAtAsc(StatusOS.RECEBIDA, pageable)
        .map(mapper::toDomain);
  }

  @Override
  public Page<OrdemServico> listarFilaExecucao(Pageable pageable) {
    return jpaRepository
        .findByStatusOrderByPrioridadeDescDataAprovacaoAsc(StatusOS.APROVADA, pageable)
        .map(mapper::toDomain);
  }

  @Override
  public Page<OrdemServico> listarFilaOperacional(Pageable pageable) {
    Pageable unsorted =
        pageable.isPaged()
            ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
            : pageable;
    return jpaRepository.listarFilaOperacional(unsorted).map(mapper::toDomain);
  }

  @Override
  public Page<OrdemServico> findByFilters(StatusOS status, UUID clienteId, Pageable pageable) {
    if (status != null && clienteId != null) {
      return jpaRepository
          .findByStatusAndClienteId(status, clienteId, pageable)
          .map(mapper::toDomain);
    } else if (status != null) {
      return jpaRepository.findByStatus(status, pageable).map(mapper::toDomain);
    } else if (clienteId != null) {
      return jpaRepository.findByClienteId(clienteId, pageable).map(mapper::toDomain);
    }
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public Optional<OrdemServico> findByCodigo(String codigo) {
    return jpaRepository.findByCodigo(codigo).map(mapper::toDomain);
  }

  @Override
  public long count() {
    return jpaRepository.count();
  }
}
