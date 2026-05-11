package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class MecanicoNaoEncontradoException extends ResourceNotFoundException {
  public MecanicoNaoEncontradoException(UUID id) {
    super("Mecânico não encontrado com ID: %s".formatted(id), "MECANICO_404");
  }
}
