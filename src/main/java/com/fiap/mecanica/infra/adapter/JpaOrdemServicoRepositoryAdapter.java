package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import com.fiap.mecanica.infra.mapper.OrdemServicoEntityMapper;
import java.time.LocalDateTime;
import java.util.Optional;
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
  public OrdemServico save(OrdemServico ordemServico) {
    OrdemServicoEntity entity = mapper.toEntity(ordemServico);

    // Gerencia timestamps manualmente se não usar @PrePersist/@PreUpdate
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(LocalDateTime.now());
    }
    entity.setUpdatedAt(LocalDateTime.now());

    OrdemServicoEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public Optional<OrdemServico> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<OrdemServico> findByIdWithItens(UUID id) {
    return jpaRepository.findWithItensById(id).map(mapper::toDomain);
  }

  @Override
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
