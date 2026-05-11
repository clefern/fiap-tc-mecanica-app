package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.VeiculoEntity;
import com.fiap.mecanica.infra.jpa.JpaClienteRepository;
import com.fiap.mecanica.infra.jpa.JpaVeiculoRepository;
import com.fiap.mecanica.infra.mapper.VeiculoEntityMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class JpaVeiculoRepositoryAdapter implements VeiculoRepository {

  private final JpaVeiculoRepository jpaRepository;
  private final JpaClienteRepository clienteRepository;
  private final VeiculoEntityMapper mapper;

  public JpaVeiculoRepositoryAdapter(
      JpaVeiculoRepository jpaRepository,
      JpaClienteRepository clienteRepository,
      VeiculoEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.clienteRepository = clienteRepository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Veiculo> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Veiculo> findByPlaca(PlacaVeiculo placa) {
    return jpaRepository.findByPlaca(placa.value()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByPlaca(PlacaVeiculo placa) {
    return jpaRepository.existsByPlaca(placa.value());
  }

  @Override
  public boolean existsByIdAndClienteId(UUID id, UUID clienteId) {
    return jpaRepository.existsByIdAndClienteId(id, clienteId);
  }

  @Override
  public Page<Veiculo> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable).map(mapper::toDomain);
  }

  @Override
  public Veiculo save(UUID clienteId, Veiculo veiculo) {
    ClienteEntity cliente =
        clienteRepository
            .findById(clienteId)
            .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

    VeiculoEntity entity = mapper.toEntity(veiculo, cliente);
    VeiculoEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public void deleteByPlaca(PlacaVeiculo placa) {
    jpaRepository.findByPlaca(placa.value()).ifPresent(e -> jpaRepository.deleteById(e.getId()));
  }

  @Override
  public List<Veiculo> findAllByClienteId(UUID clienteId) {
    return jpaRepository.findAllByClienteId(clienteId).stream().map(mapper::toDomain).toList();
  }
}
