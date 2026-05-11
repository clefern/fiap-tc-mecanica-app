package com.fiap.mecanica.domain.exception;

public class DocumentoJaCadastradoException extends DomainRuleException {
  public DocumentoJaCadastradoException(String documento) {
    super("Documento já cadastrado: %s".formatted(documento), "CLI-409-01");
  }
}
