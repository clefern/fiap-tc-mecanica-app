package com.fiap.mecanica.infrastructure.integration.estoque;

import com.fiap.mecanica.domain.enums.TipoItem;
import java.util.UUID;

/**
 * Interface de Gateway (Porta) para comunicação com o módulo de Estoque.
 *
 * <p>Define o contrato de integração, isolando o domínio de Orçamento dos detalhes de implementação
 * de como a baixa de estoque é realizada (seja via HTTP, mensageria ou chamada local em monolito
 * modular).
 */
public interface EstoqueGateway {

  /**
   * Solicita a baixa de estoque de um item.
   *
   * @param referenciaId ID da Peça ou Insumo.
   * @param tipo Tipo do item (PECA ou INSUMO).
   * @param quantidade Quantidade a ser baixada.
   */
  void baixarEstoque(UUID referenciaId, TipoItem tipo, int quantidade);
}
