package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoService {
  Orcamento gerarOrcamento(OrdemServico ordemServico);

  Optional<Orcamento> buscarPorId(UUID id);

  Optional<Orcamento> buscarPorCodigo(String codigo);

  Optional<Orcamento> buscarPorOrdemServico(UUID ordemServicoId);

  org.springframework.data.domain.Page<Orcamento> listarTodos(
      org.springframework.data.domain.Pageable pageable);

  void deletar(UUID id);

  // Lifecycle methods
  Orcamento aprovar(UUID id);

  Orcamento aprovarPorOsId(UUID osId);

  Orcamento reprovar(UUID id);

  Orcamento reprovarPorOsId(UUID osId);

  Orcamento cancelar(UUID id);

  void cancelarOrcamentosPendentes(UUID ordemServicoId);

  // Document Generation
  byte[] recuperarPdf(UUID id);
}
