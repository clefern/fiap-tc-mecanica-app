package com.fiap.mecanica.domain.exception;

import lombok.Getter;

@Getter
public abstract class ResourceNotFoundException extends RuntimeException implements MecanicaError {
  private final String code;

  protected ResourceNotFoundException(String message, String code) {
    super(message);
    this.code = code;
  }
}
