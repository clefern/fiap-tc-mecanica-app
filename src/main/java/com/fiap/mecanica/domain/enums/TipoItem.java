package com.fiap.mecanica.domain.enums;

public enum TipoItem {
  SERVICO("Serviço"),
  PECA("Peça"),
  INSUMO("Insumo");

  private final String descricao;

  TipoItem(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
