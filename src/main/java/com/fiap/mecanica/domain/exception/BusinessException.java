package com.fiap.mecanica.domain.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException implements MecanicaError {
  private final String code;

  protected BusinessException(String message, String code) {
    super(message);
    this.code = code;
  }
}
