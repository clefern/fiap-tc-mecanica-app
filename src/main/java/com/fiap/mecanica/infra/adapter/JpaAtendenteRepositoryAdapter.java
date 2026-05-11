package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.repository.AtendenteRepository;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.infra.entity.AtendenteEntity;
import com.fiap.mecanica.infra.jpa.JpaAtendenteRepository;
import com.fiap.mecanica.infra.mapper.AtendenteEntityMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class JpaAtendenteRepositoryAdapter implements AtendenteRepository {

  private final JpaAtendenteRepository jpaRepository;
  private final AtendenteEntityMapper mapper;

  public JpaAtendenteRepositoryAdapter(
      JpaAtendenteRepository jpaRepository, AtendenteEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Atendente> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Atendente> findByCpf(CPF cpf) {
    return jpaRepository.findByCpf(cpf.valor()).map(mapper::toDomain);
  }

  @Override
  public Page<Atendente> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public boolean existsByCpf(CPF cpf) {
    return jpaRepository.existsByCpf(cpf.valor());
  }

  @Override
  public Atendente save(Atendente atendente) {
    AtendenteEntity entity = mapper.toEntity(atendente);
    AtendenteEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
