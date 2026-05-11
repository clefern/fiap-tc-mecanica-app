package com.fiap.mecanica.domain.exception;

public class DuplicatePlacaException extends BusinessException {
  public DuplicatePlacaException(String placa) {
    super("Placa já cadastrada: " + placa, "DUPLICATE_PLACA");
  }
}
