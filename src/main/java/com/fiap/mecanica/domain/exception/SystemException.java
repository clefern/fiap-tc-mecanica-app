package com.fiap.mecanica.domain.exception;

import lombok.Getter;

@Getter
public abstract class SystemException extends RuntimeException implements MecanicaError {
  private final String code;

  protected SystemException(String message, String code) {
    super(message);
    this.code = code;
  }

  protected SystemException(String message, String code, Throwable cause) {
    super(message, cause);
    this.code = code;
  }
}
