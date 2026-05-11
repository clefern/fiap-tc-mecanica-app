package com.fiap.mecanica.domain.enums;

public enum CategoriaServico {
  MANUTENCAO_PREVENTIVA("Manutenção Preventiva"),
  REPARO_MECANICO("Reparo Mecânico"),
  ELETRICA("Elétrica"),
  DIAGNOSTICO("Diagnóstico"),
  ESTETICA("Estética"),
  OUTROS("Outros");

  private final String descricao;

  CategoriaServico(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
