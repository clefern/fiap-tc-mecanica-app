package com.fiap.mecanica.infrastructure.integration.estoque.impl;

import com.fiap.mecanica.application.service.estoque.EstoqueService;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.infrastructure.integration.estoque.EstoqueGateway;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptador de Integração Local para o módulo de Estoque.
 *
 * <p>Implementa a comunicação direta (in-process) com o EstoqueService, eliminando a latência de
 * rede de chamadas HTTP internas, mas mantendo o desacoplamento lógico através da interface
 * EstoqueGateway.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstoqueModuleAdapter implements EstoqueGateway {

  private final EstoqueService estoqueService;

  @Override
  public void baixarEstoque(UUID referenciaId, TipoItem tipo, int quantidade) {
    log.info(
        "Processando baixa de estoque via adaptador local (Modular Monolith). Item: {}, Qtd: {}",
        referenciaId,
        quantidade);

    try {
      // Chamada direta ao serviço de aplicação do outro módulo
      estoqueService.baixarEstoque(referenciaId, tipo, quantidade);
      log.info("Baixa de estoque confirmada localmente.");
    } catch (Exception e) {
      log.error("❌ Falha ao processar baixa de estoque no módulo local: {}", e.getMessage());
      // Re-throw para garantir que a transação do orçamento seja revertida
      throw e;
    }
  }
}
