package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.repository.InsumoRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class InsumoServiceImpl implements InsumoService {

  private final InsumoRepository repository;

  public InsumoServiceImpl(InsumoRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  @CacheEvict(value = "insumos", allEntries = true)
  public Insumo create(Insumo insumo) {
    log.info("Criando novo insumo: {}", insumo.getNome());
    return repository.save(insumo);
  }

  @Override
  @Transactional
  @CacheEvict(value = "insumos", allEntries = true)
  public Insumo update(UUID id, Insumo insumo) {
    log.info("Atualizando insumo ID: {}", id);
    return repository
        .findById(id)
        .map(
            existing -> {
              existing.atualizar(insumo);
              return repository.save(existing);
            })
        .orElseThrow(
            () -> {
              log.error("❌ Tentativa de atualizar insumo inexistente ID: {}", id);
              return new IllegalArgumentException("Insumo não encontrado: " + id);
            });
  }

  @Override
  public Optional<Insumo> getById(UUID id) {
    log.debug("Buscando insumo por ID: {}", id);
    return repository.findById(id);
  }

  @Override
  @Cacheable(value = "insumos", key = "'all-' + #pageable")
  public Page<Insumo> getAll(Pageable pageable) {
    log.debug("Listando todos os insumos");
    return repository.findAll(pageable);
  }

  @Override
  @Cacheable(value = "insumos", key = "'ativos-' + #pageable")
  public Page<Insumo> getAllAtivos(Pageable pageable) {
    log.debug("Listando insumos ativos");
    return repository.findByAtivoTrue(pageable);
  }

  @Override
  public Page<Insumo> search(String termo, Pageable pageable) {
    log.debug("Buscando insumos por termo de pesquisa: {}", termo);
    return repository.search(termo, pageable);
  }

  @Override
  @Transactional
  @CacheEvict(value = "insumos", allEntries = true)
  public void delete(UUID id) {
    log.info("Excluindo insumo ID: {}", id);
    repository.delete(id);
  }

  @Override
  @Transactional
  @CacheEvict(value = "insumos", allEntries = true)
  public Insumo registrarBaixaEstoque(UUID id, int quantidade) {
    log.info("Registrando baixa de estoque para insumo ID={}, quantidade={}", id, quantidade);
    Insumo insumo =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error("❌ Tentativa de baixar estoque de insumo inexistente ID={}", id);
                  return new IllegalArgumentException("Insumo não encontrado: " + id);
                });

    int antes = insumo.getQuantidadeEstoque();
    insumo.baixarEstoque(quantidade);
    repository.save(insumo);
    log.info(
        "Baixa de estoque aplicada em insumo ID={}, antes={}, baixado={}, depois={}",
        id,
        antes,
        quantidade,
        insumo.getQuantidadeEstoque());
    return insumo;
  }

  @Override
  @Transactional
  @CacheEvict(value = "insumos", allEntries = true)
  public Insumo registrarEntradaEstoque(UUID id, int quantidade) {
    log.info("Registrando entrada de estoque para insumo ID={}, quantidade={}", id, quantidade);
    Insumo insumo =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error("❌ Tentativa de entrada de estoque para insumo inexistente ID={}", id);
                  return new IllegalArgumentException("Insumo não encontrado: " + id);
                });

    int antes = insumo.getQuantidadeEstoque();
    insumo.adicionarEstoque(quantidade);
    repository.save(insumo);
    log.info(
        "Entrada de estoque aplicada em insumo ID={}, antes={}, adicionado={}, depois={}",
        id,
        antes,
        quantidade,
        insumo.getQuantidadeEstoque());
    return insumo;
  }

  @Override
  @Transactional
  @CacheEvict(value = "insumos", allEntries = true)
  public Insumo atualizarParametrosEstoque(UUID id, Integer estoqueMinimo, Integer estoqueMaximo) {
    log.info(
        "Atualizando parâmetros de estoque do insumo ID={}, minimo={}, maximo={}",
        id,
        estoqueMinimo,
        estoqueMaximo);
    Insumo insumo =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error(
                      "❌ Tentativa de atualizar parâmetros de estoque de insumo inexistente ID={}",
                      id);
                  return new IllegalArgumentException("Insumo não encontrado: " + id);
                });

    insumo.atualizarEstoque(null, estoqueMinimo, estoqueMaximo);
    repository.save(insumo);
    log.debug(
        "Parâmetros de estoque atualizados para insumo ID={}, novoMinimo={}, novoMaximo={}",
        id,
        insumo.getEstoqueMinimo(),
        insumo.getEstoqueMaximo());
    return insumo;
  }
}
