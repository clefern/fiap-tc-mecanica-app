package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class ItemDuplicadoException extends BusinessException {
  public ItemDuplicadoException(String descricao, UUID referenciaId) {
    super(
        "Item '%s' (Ref: %s) já existe na Ordem de Serviço".formatted(descricao, referenciaId),
        "OS-409-01");
  }
}
