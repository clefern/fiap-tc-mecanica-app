package com.fiap.mecanica.domain.exception;

public class TransicaoStatusInvalidaException extends DomainRuleException {
  public TransicaoStatusInvalidaException(String statusAtual, String novoStatus) {
    super(
        "Não é possível transitar OS de %s para %s".formatted(statusAtual, novoStatus),
        "OS-422-01");
  }

  public TransicaoStatusInvalidaException(String message) {
    super(message, "OS-422-01");
  }
}
