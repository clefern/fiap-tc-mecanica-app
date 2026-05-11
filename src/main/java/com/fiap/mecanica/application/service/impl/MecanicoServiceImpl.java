package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.MecanicoService;
import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.domain.factory.DocumentoFactory;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.repository.MecanicoRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import com.fiap.mecanica.domain.service.PasswordPolicy;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Documento;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MecanicoServiceImpl implements MecanicoService {

  private final MecanicoRepository repo;
  private final PasswordEncoder passwordEncoder;
  private final NotificationService notificationService;
  private final PasswordPolicy passwordPolicy;

  public MecanicoServiceImpl(
      MecanicoRepository repo,
      PasswordEncoder passwordEncoder,
      NotificationService notificationService,
      PasswordPolicy passwordPolicy) {
    this.repo = repo;
    this.passwordEncoder = passwordEncoder;
    this.notificationService = notificationService;
    this.passwordPolicy = passwordPolicy;
  }

  @Override
  @Transactional
  @CacheEvict(value = "mecanicos", allEntries = true)
  public Mecanico create(Mecanico mecanico) {
    log.info("[MECANICO_CRIAR] CPF={}", mecanico.getCpf().valor());
    if (repo.existsByCpf(mecanico.getCpf())) {
      throw new DuplicateDocumentoException(mecanico.getCpf().valor());
    }

    String rawPassword = passwordPolicy.generateRandomPassword();
    mecanico.setPassword(passwordEncoder.encode(rawPassword));

    Mecanico saved = repo.save(mecanico);
    notificationService.sendWelcomeEmail(saved, rawPassword);
    log.info("[MECANICO_CRIADO] ID={}", saved.getId());

    return saved;
  }

  @Override
  @Transactional
  @CacheEvict(value = "mecanicos", allEntries = true)
  public Mecanico update(UUID id, Mecanico mecanico) {
    log.info("[MECANICO_ATUALIZAR] ID={}", id);
    if (repo.findById(id).isEmpty()) {
      throw new MecanicoNaoEncontradoException(id);
    }
    Optional<Mecanico> byCpf = repo.findByCpf(mecanico.getCpf());
    if (byCpf.isPresent() && !byCpf.get().getId().equals(id)) {
      throw new DuplicateDocumentoException(mecanico.getCpf().valor());
    }
    mecanico.setId(id);
    Mecanico saved = repo.save(mecanico);
    log.info("[MECANICO_ATUALIZADO] ID={}", saved.getId());
    return saved;
  }

  @Override
  public Optional<Mecanico> getById(UUID id) {
    log.debug("[MECANICO_BUSCAR_POR_ID] ID={}", id);
    return repo.findById(id);
  }

  @Override
  public Optional<Mecanico> getByCpf(String cpf) {
    log.debug("[MECANICO_BUSCAR_POR_CPF] CPF={}", cpf);
    Documento doc = DocumentoFactory.create(cpf);
    if (!(doc instanceof CPF)) {
      throw new IllegalArgumentException("Documento deve ser um CPF válido");
    }
    return repo.findByCpf((CPF) doc);
  }

  @Override
  @Cacheable(value = "mecanicos", key = "#pageable")
  public Page<Mecanico> getAll(Pageable pageable) {
    log.debug("[MECANICO_LISTAR] Pageable={}", pageable);
    return repo.findAll(pageable);
  }

  @Override
  @Transactional
  @CacheEvict(value = "mecanicos", allEntries = true)
  public void delete(UUID id) {
    log.info("[MECANICO_REMOVER] ID={}", id);
    repo.deleteById(id);
  }
}
