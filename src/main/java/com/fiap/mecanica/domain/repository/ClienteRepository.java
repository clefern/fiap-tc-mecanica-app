package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.Documento;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port de repositório para Cliente (Camada de Domínio). Implementações ficam na camada de Infra.
 */
public interface ClienteRepository {

  Optional<Cliente> findById(UUID id);

  Optional<Cliente> findByDocumento(Documento documento);

  Page<Cliente> findAll(Pageable pageable);

  boolean existsByDocumento(Documento documento);

  boolean existsById(UUID id);

  /** Persiste Cliente e retorna a entidade atualizada (ex.: com ID atribuído). */
  Cliente save(Cliente cliente);

  void deleteById(UUID id);
}
