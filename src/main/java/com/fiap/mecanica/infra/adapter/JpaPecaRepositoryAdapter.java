package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.repository.PecaRepository;
import com.fiap.mecanica.infra.entity.PecaEntity;
import com.fiap.mecanica.infra.jpa.JpaPecaRepository;
import com.fiap.mecanica.infra.mapper.PecaEntityMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class JpaPecaRepositoryAdapter implements PecaRepository {

  private final JpaPecaRepository jpaRepository;
  private final PecaEntityMapper mapper;

  public JpaPecaRepositoryAdapter(JpaPecaRepository jpaRepository, PecaEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Peca save(Peca peca) {
    PecaEntity entity = mapper.toEntity(peca);
    PecaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Peca> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Page<Peca> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public Page<Peca> findByAtivoTrue(Pageable pageable) {
    return jpaRepository.findByAtivoTrue(pageable).map(mapper::toDomain);
  }

  @Override
  public Page<Peca> search(String termo, Pageable pageable) {
    return jpaRepository.search(termo, pageable).map(mapper::toDomain);
  }

  @Override
  public List<Peca> findItensComEstoqueBaixo() {
    return jpaRepository.findEstoqueBaixo().stream().map(mapper::toDomain).toList();
  }

  @Override
  public void delete(UUID id) {
    jpaRepository.deleteById(id);
  }
}
