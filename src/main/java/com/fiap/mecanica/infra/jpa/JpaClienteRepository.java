package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.ClienteEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaClienteRepository extends JpaRepository<ClienteEntity, UUID> {
  Optional<ClienteEntity> findByDocumento(String documento);

  boolean existsByDocumento(String documento);
}
