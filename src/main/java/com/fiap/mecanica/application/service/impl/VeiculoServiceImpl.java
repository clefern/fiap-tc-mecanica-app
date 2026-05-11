package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.VeiculoService;
import com.fiap.mecanica.domain.exception.DuplicatePlacaException;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class VeiculoServiceImpl implements VeiculoService {

  private final VeiculoRepository repo;

  public VeiculoServiceImpl(VeiculoRepository repo) {
    this.repo = repo;
  }

  @Override
  @Transactional
  @CacheEvict(value = "veiculos", allEntries = true)
  public Veiculo create(UUID clienteId, Veiculo novo) {
    log.info("[VEICULO_CRIAR] ClienteId={} Placa={}", clienteId, novo.getPlaca().value());
    if (repo.existsByPlaca(novo.getPlaca())) {
      throw new DuplicatePlacaException(novo.getPlaca().value());
    }
    Veiculo saved = repo.save(clienteId, novo);
    log.info(
        "[VEICULO_CRIADO] ID={} ClienteId={} Placa={}",
        saved.getId(),
        clienteId,
        saved.getPlaca().value());
    return saved;
  }

  @Override
  public Optional<Veiculo> getByPlaca(String placa) {
    log.debug("[VEICULO_BUSCAR_POR_PLACA] Placa={}", placa);
    return repo.findByPlaca(PlacaVeiculo.of(placa));
  }

  @Override
  @Transactional
  @CacheEvict(value = "veiculos", allEntries = true)
  public void deleteByPlaca(String placa) {
    log.info("[VEICULO_REMOVER_POR_PLACA] Placa={}", placa);
    repo.deleteByPlaca(PlacaVeiculo.of(placa));
  }

  @Override
  @Cacheable(value = "veiculos", key = "#clienteId")
  public List<Veiculo> listByClienteId(UUID clienteId) {
    log.debug("[VEICULO_LISTAR_POR_CLIENTE] ClienteId={}", clienteId);
    return repo.findAllByClienteId(clienteId);
  }

  @Override
  @Cacheable(value = "veiculos", key = "#pageable")
  public org.springframework.data.domain.Page<Veiculo> getAll(
      org.springframework.data.domain.Pageable pageable) {
    log.debug("[VEICULO_LISTAR_TODOS] Pageable={}", pageable);
    return repo.findAll(pageable);
  }
}
