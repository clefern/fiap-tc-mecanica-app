package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdemServicoService {
  OrdemServico criarOrdemServico(UUID clienteId, UUID veiculoId, String observacoes);

  OrdemServico abrirOsCompleta(
      UUID clienteId, UUID veiculoId, String observacoes, List<ItemOrdemServico> itens);

  OrdemServico buscarPorId(UUID id);

  OrdemServico buscarPorCodigo(String codigo);

  Page<OrdemServico> listarTodas(StatusOS status, UUID clienteId, Pageable pageable);

  Page<OrdemServico> listarFilaOperacional(Pageable pageable);
}
