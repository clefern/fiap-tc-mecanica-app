package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.List;
import java.util.UUID;

public interface OsItemService {
  OrdemServico adicionarItem(UUID id, ItemOrdemServico item, UUID mecanicoId);

  OrdemServico adicionarItensEmLote(UUID id, List<ItemOrdemServico> itens, UUID mecanicoId);

  OrdemServico atualizarQuantidadeItem(
      UUID id, UUID itemId, Integer novaQuantidade, UUID mecanicoId);

  OrdemServico removerItem(UUID id, UUID itemId, UUID mecanicoId);
}
