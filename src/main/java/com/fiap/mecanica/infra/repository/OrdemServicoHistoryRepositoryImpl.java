package com.fiap.mecanica.infra.repository;

import com.fiap.mecanica.domain.repository.OrdemServicoHistoryRepository;
import com.fiap.mecanica.infra.entity.OrdemServicoHistory;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoHistoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrdemServicoHistoryRepositoryImpl implements OrdemServicoHistoryRepository {

  private final JpaOrdemServicoHistoryRepository jpaRepository;

  @Override
  public OrdemServicoHistory save(OrdemServicoHistory history) {
    return jpaRepository.save(history);
  }

  @Override
  public Optional<OrdemServicoHistory> findLatestByOrdemServicoId(UUID ordemServicoId) {
    return jpaRepository.findActiveByOrdemServicoId(ordemServicoId).stream().findFirst();
  }

  @Override
  public List<OrdemServicoHistory> findByOrdemServicoId(UUID ordemServicoId) {
    return jpaRepository.findByOrdemServicoIdOrderByStartedAtAsc(ordemServicoId);
  }
}
