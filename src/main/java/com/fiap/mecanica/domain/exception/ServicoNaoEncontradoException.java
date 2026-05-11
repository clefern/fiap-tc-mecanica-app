package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class ServicoNaoEncontradoException extends ResourceNotFoundException {
  public ServicoNaoEncontradoException(UUID id) {
    super("Serviço não encontrado com ID: %s".formatted(id), "SERVICO_404");
  }
}
