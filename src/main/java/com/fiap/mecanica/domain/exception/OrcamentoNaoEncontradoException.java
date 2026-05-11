package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class OrcamentoNaoEncontradoException extends ResourceNotFoundException {
  public OrcamentoNaoEncontradoException(UUID id) {
    super("Orçamento não encontrado com ID: %s".formatted(id), "ORC-404");
  }

  public OrcamentoNaoEncontradoException(String codigo) {
    super("Orçamento não encontrado com código: %s".formatted(codigo), "ORC-404");
  }
}
