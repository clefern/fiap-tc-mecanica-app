package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class ClienteNaoEncontradoException extends ResourceNotFoundException {
  public ClienteNaoEncontradoException(UUID id) {
    super("Cliente não encontrado com ID: %s".formatted(id), "CLIENTE_404");
  }
}
