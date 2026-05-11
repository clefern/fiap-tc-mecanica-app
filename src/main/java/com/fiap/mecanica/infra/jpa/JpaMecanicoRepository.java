package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.MecanicoEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMecanicoRepository extends JpaRepository<MecanicoEntity, UUID> {
  Optional<MecanicoEntity> findByCpf(String cpf);

  boolean existsByCpf(String cpf);
}
