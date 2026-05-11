package com.fiap.mecanica.domain.exception;

import lombok.Getter;

@Getter
public abstract class MecanicaException extends RuntimeException implements MecanicaError {
  private final String code;

  protected MecanicaException(String message, String code) {
    super(message);
    this.code = code;
  }

  protected MecanicaException(String message, String code, Throwable cause) {
    super(message, cause);
    this.code = code;
  }
}
