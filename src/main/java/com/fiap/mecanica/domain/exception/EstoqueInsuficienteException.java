package com.fiap.mecanica.domain.exception;

import java.util.UUID;

public class EstoqueInsuficienteException extends DomainRuleException {
  public EstoqueInsuficienteException(
      UUID pecaId, Integer quantidadeSolicitada, Integer saldoDisponivel) {
    this(pecaId.toString(), quantidadeSolicitada, saldoDisponivel);
  }

  public EstoqueInsuficienteException(
      String nomeItem, Integer quantidadeSolicitada, Integer saldoDisponivel) {
    super(
        "Estoque insuficiente para o item '%s'. Solicitado: %d, Disponível: %d"
            .formatted(nomeItem, quantidadeSolicitada, saldoDisponivel),
        "STK-422-01");
  }
}
