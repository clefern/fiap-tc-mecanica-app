package com.fiap.mecanica.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ViolacaoPrioridadeException extends BusinessException {

  public ViolacaoPrioridadeException(String message) {
    super(message, "VIOLACAO_PRIORIDADE");
  }
}
