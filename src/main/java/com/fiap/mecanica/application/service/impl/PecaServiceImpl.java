package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.repository.PecaRepository;
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
public class PecaServiceImpl implements PecaService {

  private final PecaRepository repository;

  public PecaServiceImpl(PecaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  @CacheEvict(value = "pecas", allEntries = true)
  public Peca create(Peca peca) {
    log.info("Criando nova peça: {}", peca.getNome());
    return repository.save(peca);
  }

  @Override
  @Transactional
  @CacheEvict(value = "pecas", allEntries = true)
  public Peca update(UUID id, Peca peca) {
    log.info("Atualizando peça ID: {}", id);
    return repository
        .findById(id)
        .map(
            existing -> {
              existing.atualizar(peca);
              return repository.save(existing);
            })
        .orElseThrow(
            () -> {
              log.error("❌ Tentativa de atualizar peça inexistente ID: {}", id);
              return new IllegalArgumentException("Peça não encontrada: " + id);
            });
  }

  @Override
  public Optional<Peca> getById(UUID id) {
    log.debug("Buscando peça por ID: {}", id);
    return repository.findById(id);
  }

  @Override
  @Cacheable(value = "pecas", key = "#pageable")
  public Page<Peca> getAll(Pageable pageable) {
    log.debug("Listando todas as peças");
    return repository.findAll(pageable);
  }

  @Override
  @Cacheable(value = "pecas", key = "'ativos-' + #pageable")
  public Page<Peca> getAllAtivos(Pageable pageable) {
    log.debug("Listando peças ativas");
    return repository.findByAtivoTrue(pageable);
  }

  @Override
  public Page<Peca> search(String termo, Pageable pageable) {
    log.debug("Buscando peças por termo de pesquisa: {}", termo);
    return repository.search(termo, pageable);
  }

  @Override
  @Transactional
  @CacheEvict(value = "pecas", allEntries = true)
  public void delete(UUID id) {
    log.info("Excluindo peça ID: {}", id);
    repository.delete(id);
  }

  @Override
  @Transactional
  @CacheEvict(value = "pecas", allEntries = true)
  public Peca registrarBaixaEstoque(UUID id, int quantidade) {
    log.info("Registrando baixa de estoque para peça ID={}, quantidade={}", id, quantidade);
    Peca peca =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error("❌ Tentativa de baixar estoque de peça inexistente ID={}", id);
                  return new IllegalArgumentException("Peça não encontrada: " + id);
                });

    int antes = peca.getQuantidadeEstoque();
    peca.baixarEstoque(quantidade);
    repository.save(peca);
    log.info(
        "Baixa de estoque aplicada em peça ID={}, antes={}, baixado={}, depois={}",
        id,
        antes,
        quantidade,
        peca.getQuantidadeEstoque());
    return peca;
  }

  @Override
  @Transactional
  @CacheEvict(value = "pecas", allEntries = true)
  public Peca registrarEntradaEstoque(UUID id, int quantidade) {
    log.info("Registrando entrada de estoque para peça ID={}, quantidade={}", id, quantidade);
    Peca peca =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error("❌ Tentativa de entrada de estoque para peça inexistente ID={}", id);
                  return new IllegalArgumentException("Peça não encontrada: " + id);
                });

    int antes = peca.getQuantidadeEstoque();
    peca.adicionarEstoque(quantidade);
    repository.save(peca);
    log.info(
        "Entrada de estoque aplicada em peça ID={}, antes={}, adicionado={}, depois={}",
        id,
        antes,
        quantidade,
        peca.getQuantidadeEstoque());
    return peca;
  }

  @Override
  @Transactional
  @CacheEvict(value = "pecas", allEntries = true)
  public Peca atualizarParametrosEstoque(UUID id, Integer estoqueMinimo, Integer estoqueMaximo) {
    log.info(
        "Atualizando parâmetros de estoque da peça ID={}, minimo={}, maximo={}",
        id,
        estoqueMinimo,
        estoqueMaximo);
    Peca peca =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error(
                      "❌ Tentativa de atualizar parâmetros de estoque de peça inexistente ID={}",
                      id);
                  return new IllegalArgumentException("Peça não encontrada: " + id);
                });

    peca.atualizarEstoque(null, estoqueMinimo, estoqueMaximo);
    repository.save(peca);
    log.debug(
        "Parâmetros de estoque atualizados para peça ID={}, novoMinimo={}, novoMaximo={}",
        id,
        peca.getEstoqueMinimo(),
        peca.getEstoqueMaximo());
    return peca;
  }
}
