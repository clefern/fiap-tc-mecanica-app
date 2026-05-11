package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.ServicoService;
import com.fiap.mecanica.domain.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.domain.repository.ServicoRepository;
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
public class ServicoServiceImpl implements ServicoService {

  private final ServicoRepository repository;

  public ServicoServiceImpl(ServicoRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  @CacheEvict(value = "servicos", allEntries = true)
  public Servico create(Servico servico) {
    log.info("Criando novo serviço: {}", servico.getNome());
    return repository.save(servico);
  }

  @Override
  @Transactional
  @CacheEvict(value = "servicos", allEntries = true)
  public Servico update(UUID id, Servico servico) {
    log.info("Atualizando serviço ID: {}", id);
    return repository
        .findById(id)
        .map(
            existing -> {
              existing.atualizar(
                  servico.getNome(),
                  servico.getDescricao(),
                  servico.getPrecoBase(),
                  servico.getTempoEstimado(),
                  servico.getCategoria(),
                  servico.isAtivo());
              return repository.save(existing);
            })
        .orElseThrow(
            () -> {
              log.error("❌ Tentativa de atualizar serviço inexistente ID: {}", id);
              return new ServicoNaoEncontradoException(id);
            });
  }

  @Override
  public Optional<Servico> getById(UUID id) {
    log.debug("Buscando serviço por ID: {}", id);
    return repository.findById(id);
  }

  @Override
  @Cacheable(value = "servicos", key = "#pageable")
  public Page<Servico> getAll(Pageable pageable) {
    log.debug("Listando todos os serviços");
    return repository.findAll(pageable);
  }

  @Override
  @Cacheable(value = "servicos", key = "'ativos-' + #pageable")
  public Page<Servico> getAllAtivos(Pageable pageable) {
    log.debug("Listando serviços ativos");
    return repository.findByAtivoTrue(pageable);
  }

  @Override
  @Transactional
  @CacheEvict(value = "servicos", allEntries = true)
  public void delete(UUID id) {
    log.info("Excluindo serviço ID: {}", id);
    repository.delete(id);
  }
}
