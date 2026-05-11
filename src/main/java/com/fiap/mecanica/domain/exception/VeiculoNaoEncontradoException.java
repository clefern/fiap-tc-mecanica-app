package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class VeiculoNaoEncontradoException extends ResourceNotFoundException {
  public VeiculoNaoEncontradoException(UUID id) {
    super("Veículo não encontrado com ID: %s".formatted(id), "VEICULO_404");
  }

  public VeiculoNaoEncontradoException(String message) {
    super(message, "VEICULO_404");
  }
}
