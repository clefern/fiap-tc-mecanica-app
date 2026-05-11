package com.fiap.mecanica.application.service.prioridade;

import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PrioridadeService {
  Page<OrdemServico> listarFilaOrcamento(Pageable pageable);

  Page<OrdemServico> listarFilaExecucao(Pageable pageable);

  Optional<OrdemServico> obterProximaParaOrcamento();

  Optional<OrdemServico> obterProximaParaExecucao();

  void validarPrioridadeOrcamento(UUID osId);

  void validarPrioridadeExecucao(UUID osId);

  OrdemServico atualizarPrioridade(
      UUID id, com.fiap.mecanica.domain.enums.Prioridade novaPrioridade);
}
