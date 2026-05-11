package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.AtendenteEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAtendenteRepository extends JpaRepository<AtendenteEntity, UUID> {
  Optional<AtendenteEntity> findByCpf(String cpf);

  boolean existsByCpf(String cpf);
}
