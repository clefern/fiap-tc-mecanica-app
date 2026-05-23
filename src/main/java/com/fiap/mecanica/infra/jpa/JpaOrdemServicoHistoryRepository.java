package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.OrdemServicoHistory;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOrdemServicoHistoryRepository extends JpaRepository<OrdemServicoHistory, UUID> {

	@Query("""
		SELECT h FROM OrdemServicoHistory h
		WHERE h.ordemServicoId = :osId
		  AND h.endedAt IS NULL
		ORDER BY h.startedAt DESC
		""")
	List<OrdemServicoHistory> findActiveByOrdemServicoId(@Param("osId") UUID osId);

	List<OrdemServicoHistory> findByOrdemServicoIdOrderByStartedAtAsc(UUID ordemServicoId);
}
