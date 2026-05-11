package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.model.Orcamento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrcamentoRepository {
  Orcamento save(Orcamento orcamento);

  Optional<Orcamento> findById(UUID id);

  Optional<Orcamento> findByCodigo(String codigo);

  Optional<Orcamento> findByOrdemServicoId(UUID ordemServicoId);

  List<Orcamento> findAllByOrdemServicoId(UUID ordemServicoId);

  Optional<Orcamento> findByOrdemServicoIdAndStatus(UUID ordemServicoId, StatusOrcamento status);

  boolean existsByOrdemServicoIdAndStatus(UUID ordemServicoId, StatusOrcamento status);

  Page<Orcamento> findAll(Pageable pageable);

  void deleteById(UUID id);
}
