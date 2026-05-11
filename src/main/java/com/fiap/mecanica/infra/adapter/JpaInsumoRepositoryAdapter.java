package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.repository.InsumoRepository;
import com.fiap.mecanica.infra.entity.InsumoEntity;
import com.fiap.mecanica.infra.jpa.JpaInsumoRepository;
import com.fiap.mecanica.infra.mapper.InsumoEntityMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class JpaInsumoRepositoryAdapter implements InsumoRepository {

  private final JpaInsumoRepository jpaRepository;
  private final InsumoEntityMapper mapper;

  public JpaInsumoRepositoryAdapter(JpaInsumoRepository jpaRepository, InsumoEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Insumo save(Insumo insumo) {
    InsumoEntity entity = mapper.toEntity(insumo);
    InsumoEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Insumo> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Page<Insumo> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public Page<Insumo> findByAtivoTrue(Pageable pageable) {
    return jpaRepository.findByAtivoTrue(pageable).map(mapper::toDomain);
  }

  @Override
  public Page<Insumo> search(String termo, Pageable pageable) {
    return jpaRepository.search(termo, pageable).map(mapper::toDomain);
  }

  @Override
  public List<Insumo> findItensComEstoqueBaixo() {
    return jpaRepository.findEstoqueBaixo().stream().map(mapper::toDomain).toList();
  }

  @Override
  public void delete(UUID id) {
    jpaRepository.deleteById(id);
  }
}
