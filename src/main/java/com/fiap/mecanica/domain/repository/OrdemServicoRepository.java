package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdemServicoRepository extends BaseRepository {
  OrdemServico save(OrdemServico ordemServico);

  Optional<OrdemServico> findById(UUID id);

  Optional<OrdemServico> findByIdWithItens(UUID id);

  Page<OrdemServico> findAll(Pageable pageable);

  Page<OrdemServico> findByFilters(StatusOS status, UUID clienteId, Pageable pageable);

  Page<OrdemServico> listarFilaOrcamento(Pageable pageable);

  Page<OrdemServico> listarFilaExecucao(Pageable pageable);

  Page<OrdemServico> listarFilaOperacional(Pageable pageable);

  Optional<OrdemServico> findByCodigo(String codigo);

  long count();
  // Métodos para gerar código sequencial podem ser necessários
}
