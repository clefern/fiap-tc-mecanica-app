package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class AtendenteNaoEncontradoException extends ResourceNotFoundException {
  public AtendenteNaoEncontradoException(UUID id) {
    super("Atendente não encontrado com ID: %s".formatted(id), "ATENDENTE_404");
  }

  public AtendenteNaoEncontradoException(String message) {
    super(message, "ATENDENTE_404");
  }
}
