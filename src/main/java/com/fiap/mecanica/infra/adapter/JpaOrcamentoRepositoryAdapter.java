package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.repository.OrcamentoRepository;
import com.fiap.mecanica.infra.entity.OrcamentoEntity;
import com.fiap.mecanica.infra.jpa.JpaOrcamentoRepository;
import com.fiap.mecanica.infra.mapper.OrcamentoEntityMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaOrcamentoRepositoryAdapter implements OrcamentoRepository {

  private final JpaOrcamentoRepository jpaRepository;
  private final OrcamentoEntityMapper mapper;

  @Override
  @Transactional
  public Orcamento save(Orcamento orcamento) {
    OrcamentoEntity entity = mapper.toEntity(orcamento);
    OrcamentoEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Orcamento> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Orcamento> findByCodigo(String codigo) {
    return jpaRepository.findByCodigo(codigo).map(mapper::toDomain);
  }

  @Override
  public Optional<Orcamento> findByOrdemServicoId(UUID ordemServicoId) {
    // Mantém compatibilidade mas prefira usar findAllByOrdemServicoId
    return jpaRepository.findByOrdemServicoId(ordemServicoId).map(mapper::toDomain);
  }

  @Override
  public List<Orcamento> findAllByOrdemServicoId(UUID ordemServicoId) {
    return jpaRepository.findAllByOrdemServicoId(ordemServicoId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Orcamento> findByOrdemServicoIdAndStatus(
      UUID ordemServicoId, StatusOrcamento status) {
    return jpaRepository
        .findByOrdemServicoIdAndStatus(ordemServicoId, status)
        .map(mapper::toDomain);
  }

  @Override
  public boolean existsByOrdemServicoIdAndStatus(UUID ordemServicoId, StatusOrcamento status) {
    return jpaRepository.existsByOrdemServicoIdAndStatus(ordemServicoId, status);
  }

  @Override
  public org.springframework.data.domain.Page<Orcamento> findAll(
      org.springframework.data.domain.Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  @Transactional
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
