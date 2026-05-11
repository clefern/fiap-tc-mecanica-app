package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.domain.repository.ServicoRepository;
import com.fiap.mecanica.infra.entity.ServicoEntity;
import com.fiap.mecanica.infra.jpa.JpaServicoRepository;
import com.fiap.mecanica.infra.mapper.ServicoEntityMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class JpaServicoRepositoryAdapter implements ServicoRepository {

  private final JpaServicoRepository jpaRepository;
  private final ServicoEntityMapper mapper;

  public JpaServicoRepositoryAdapter(
      JpaServicoRepository jpaRepository, ServicoEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Servico save(Servico servico) {
    ServicoEntity entity = mapper.toEntity(servico);
    ServicoEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Servico> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Page<Servico> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public Page<Servico> findByAtivoTrue(Pageable pageable) {
    return jpaRepository.findByAtivoTrue(pageable).map(mapper::toDomain);
  }

  @Override
  public void delete(UUID id) {
    jpaRepository.deleteById(id);
  }
}
