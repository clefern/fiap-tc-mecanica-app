package com.fiap.mecanica.application.service.estoque;

import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.ItemEstocavel;
import java.util.UUID;

public interface EstoqueService {

  ItemEstocavel baixarEstoque(UUID referenciaId, TipoItem tipo, int quantidade);

  ItemEstocavel adicionarEstoque(UUID referenciaId, TipoItem tipo, int quantidade);

  ItemEstocavel atualizarParametrosEstoque(
      UUID referenciaId, TipoItem tipo, Integer estoqueMinimo, Integer estoqueMaximo);
}
