package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.infra.entity.OrcamentoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrcamentoRepository extends JpaRepository<OrcamentoEntity, UUID> {
  Optional<OrcamentoEntity> findByCodigo(String codigo);

  Optional<OrcamentoEntity> findByOrdemServicoId(UUID ordemServicoId);

  List<OrcamentoEntity> findAllByOrdemServicoId(UUID ordemServicoId);

  Optional<OrcamentoEntity> findByOrdemServicoIdAndStatus(
      UUID ordemServicoId, StatusOrcamento status);

  boolean existsByOrdemServicoIdAndStatus(UUID ordemServicoId, StatusOrcamento status);
}
