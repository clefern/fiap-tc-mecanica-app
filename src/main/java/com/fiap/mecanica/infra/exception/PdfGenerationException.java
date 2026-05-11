package com.fiap.mecanica.infra.exception;

import com.fiap.mecanica.domain.exception.SystemException;

public class PdfGenerationException extends SystemException {
  public PdfGenerationException(String message, Throwable cause) {
    super(message, "SYS-PDF-001", cause);
  }
}
