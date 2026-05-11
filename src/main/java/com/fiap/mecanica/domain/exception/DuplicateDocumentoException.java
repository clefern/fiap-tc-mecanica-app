package com.fiap.mecanica.domain.exception;

public class DuplicateDocumentoException extends BusinessException {
  public DuplicateDocumentoException(String documento) {
    super("Documento já cadastrado: " + documento, "DUPLICATE_DOC");
  }
}
