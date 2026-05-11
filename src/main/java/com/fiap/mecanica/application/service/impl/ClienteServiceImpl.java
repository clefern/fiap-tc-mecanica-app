package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.ClienteService;
import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.factory.DocumentoFactory;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import com.fiap.mecanica.domain.service.PasswordPolicy;
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
public class ClienteServiceImpl implements ClienteService {

  private final ClienteRepository repo;
  private final PasswordEncoder passwordEncoder;
  private final NotificationService notificationService;
  private final PasswordPolicy passwordPolicy;

  public ClienteServiceImpl(
      ClienteRepository repo,
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
  @CacheEvict(value = "clientes", allEntries = true)
  public Cliente create(Cliente novo) {
    log.info("[CLIENTE_CRIAR] Documento={}", novo.getDocumento().valor());
    if (repo.existsByDocumento(novo.getDocumento())) {
      throw new DuplicateDocumentoException(novo.getDocumento().valor());
    }

    String rawPassword = passwordPolicy.generateRandomPassword();
    novo.setPassword(passwordEncoder.encode(rawPassword));

    Cliente saved = repo.save(novo);
    notificationService.sendWelcomeEmail(saved, rawPassword);
    log.info("[CLIENTE_CRIADO] ID={}", saved.getId());

    return saved;
  }

  @Override
  @Transactional
  @CacheEvict(value = "clientes", allEntries = true)
  public Cliente update(UUID id, Cliente atualizado) {
    log.info("[CLIENTE_ATUALIZAR] ID={}", id);
    Optional<Cliente> byDoc = repo.findByDocumento(atualizado.getDocumento());
    if (byDoc.isPresent() && !byDoc.get().getId().equals(id)) {
      throw new DuplicateDocumentoException(atualizado.getDocumento().valor());
    }
    atualizado.setId(id);
    Cliente saved = repo.save(atualizado);
    log.info("[CLIENTE_ATUALIZADO] ID={}", saved.getId());
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Cliente> getById(UUID id) {
    log.debug("[CLIENTE_BUSCAR_POR_ID] ID={}", id);
    return repo.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Cliente> getByDocumento(String documento) {
    Documento doc = DocumentoFactory.create(documento);
    log.debug("[CLIENTE_BUSCAR_POR_DOCUMENTO] Documento={}", doc.valor());
    return repo.findByDocumento(doc);
  }

  @Override
  @Cacheable(value = "clientes", key = "#pageable")
  public Page<Cliente> getAll(Pageable pageable) {
    log.debug("[CLIENTE_LISTAR] Pageable={}", pageable);
    return repo.findAll(pageable);
  }

  @Override
  @Transactional
  @CacheEvict(value = "clientes", allEntries = true)
  public void delete(UUID id) {
    log.info("[CLIENTE_REMOVER] ID={}", id);
    repo.deleteById(id);
  }
}
