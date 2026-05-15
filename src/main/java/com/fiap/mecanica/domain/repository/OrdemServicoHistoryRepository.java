package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.infra.entity.OrdemServicoHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoHistoryRepository extends BaseRepository {
  OrdemServicoHistory save(OrdemServicoHistory history);

  Optional<OrdemServicoHistory> findLatestByOrdemServicoId(UUID ordemServicoId);

  List<OrdemServicoHistory> findByOrdemServicoId(UUID ordemServicoId);
}
