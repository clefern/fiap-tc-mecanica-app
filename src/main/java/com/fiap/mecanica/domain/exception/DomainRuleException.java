package com.fiap.mecanica.domain.exception;

import lombok.Getter;

@Getter
public abstract class DomainRuleException extends RuntimeException implements MecanicaError {
  private final String code;

  protected DomainRuleException(String message, String code) {
    super(message);
    this.code = code;
  }
}
