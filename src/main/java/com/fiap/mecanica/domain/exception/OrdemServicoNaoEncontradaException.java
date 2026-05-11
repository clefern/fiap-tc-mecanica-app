package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class OrdemServicoNaoEncontradaException extends ResourceNotFoundException {
  public OrdemServicoNaoEncontradaException(UUID id) {
    super("Ordem de Serviço não encontrada com ID: %s".formatted(id), "OS-404");
  }

  public OrdemServicoNaoEncontradaException(String codigo) {
    super("Ordem de Serviço não encontrada com código: %s".formatted(codigo), "OS-404");
  }
}
