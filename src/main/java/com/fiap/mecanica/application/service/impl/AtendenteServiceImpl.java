package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.AtendenteService;
import com.fiap.mecanica.domain.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.repository.AtendenteRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import com.fiap.mecanica.domain.service.PasswordPolicy;
import com.fiap.mecanica.domain.valueobject.CPF;
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
public class AtendenteServiceImpl implements AtendenteService {

  private final AtendenteRepository repo;
  private final PasswordEncoder passwordEncoder;
  private final NotificationService notificationService;
  private final PasswordPolicy passwordPolicy;

  public AtendenteServiceImpl(
      AtendenteRepository repo,
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
  @CacheEvict(value = "atendentes", allEntries = true)
  public Atendente create(Atendente atendente) {
    log.info("[ATENDENTE_CRIAR] CPF={}", atendente.getCpf().valor());
    if (repo.existsByCpf(atendente.getCpf())) {
      throw new DuplicateDocumentoException(atendente.getCpf().valor());
    }

    String rawPassword = passwordPolicy.generateRandomPassword();
    atendente.setPassword(passwordEncoder.encode(rawPassword));

    Atendente saved = repo.save(atendente);
    notificationService.sendWelcomeEmail(saved, rawPassword);
    log.info("[ATENDENTE_CRIADO] ID={}", saved.getId());

    return saved;
  }

  @Override
  @Transactional
  @CacheEvict(value = "atendentes", allEntries = true)
  public Atendente update(UUID id, Atendente atendente) {
    log.info("[ATENDENTE_ATUALIZAR] ID={}", id);
    if (repo.findById(id).isEmpty()) {
      throw new AtendenteNaoEncontradoException("Atendente não encontrado");
    }
    Optional<Atendente> byCpf = repo.findByCpf(atendente.getCpf());
    if (byCpf.isPresent() && !byCpf.get().getId().equals(id)) {
      throw new DuplicateDocumentoException(atendente.getCpf().valor());
    }
    atendente.setId(id);
    Atendente saved = repo.save(atendente);
    log.info("[ATENDENTE_ATUALIZADO] ID={}", saved.getId());
    return saved;
  }

  @Override
  public Optional<Atendente> getById(UUID id) {
    log.debug("[ATENDENTE_BUSCAR_POR_ID] ID={}", id);
    return repo.findById(id);
  }

  @Override
  public Optional<Atendente> getByCpf(String cpf) {
    log.debug("[ATENDENTE_BUSCAR_POR_CPF] CPF={}", cpf);
    return repo.findByCpf(CPF.of(cpf));
  }

  @Override
  @Cacheable(value = "atendentes", key = "#pageable")
  public Page<Atendente> getAll(Pageable pageable) {
    log.debug("[ATENDENTE_LISTAR] Pageable={}", pageable);
    return repo.findAll(pageable);
  }

  @Override
  @Transactional
  @CacheEvict(value = "atendentes", allEntries = true)
  public void delete(UUID id) {
    log.info("[ATENDENTE_REMOVER] ID={}", id);
    repo.deleteById(id);
  }
}
