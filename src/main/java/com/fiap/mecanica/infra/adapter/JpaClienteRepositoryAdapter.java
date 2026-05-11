package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.valueobject.*;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.jpa.JpaClienteRepository;
import com.fiap.mecanica.infra.mapper.ClienteEntityMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class JpaClienteRepositoryAdapter implements ClienteRepository {

  private final JpaClienteRepository jpaRepository;
  private final ClienteEntityMapper mapper;

  public JpaClienteRepositoryAdapter(
      JpaClienteRepository jpaRepository, ClienteEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Cliente> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Cliente> findByDocumento(Documento documento) {
    return jpaRepository.findByDocumento(documento.valor()).map(mapper::toDomain);
  }

  @Override
  public Page<Cliente> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public boolean existsByDocumento(Documento documento) {
    return jpaRepository.existsByDocumento(documento.valor());
  }

  @Override
  public boolean existsById(UUID id) {
    return jpaRepository.existsById(id);
  }

  @Override
  public Cliente save(Cliente cliente) {
    ClienteEntity entity = mapper.toEntity(cliente);
    ClienteEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
