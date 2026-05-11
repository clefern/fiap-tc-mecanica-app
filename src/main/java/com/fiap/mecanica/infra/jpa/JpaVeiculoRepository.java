package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.VeiculoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaVeiculoRepository extends JpaRepository<VeiculoEntity, UUID> {
  Optional<VeiculoEntity> findByPlaca(String placa);

  boolean existsByPlaca(String placa);

  boolean existsByIdAndClienteId(UUID id, UUID clienteId);

  List<VeiculoEntity> findAllByClienteId(UUID clienteId);

  // Explicitly expose common JpaRepository methods to satisfy compilation
  Optional<VeiculoEntity> findById(UUID id);

  VeiculoEntity save(VeiculoEntity entity);

  void deleteById(UUID id);
}
