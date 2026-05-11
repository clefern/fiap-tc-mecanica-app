package com.fiap.mecanica.domain.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class Insumo extends ItemEstocavel {

  private String unidadeMedida;

  public Insumo(
      UUID id,
      String nome,
      String descricao,
      BigDecimal precoBase,
      boolean ativo,
      String unidadeMedida,
      Integer quantidadeEstoque,
      Integer estoqueMinimo,
      Integer estoqueMaximo) {
    super(id, nome, descricao, precoBase, ativo, quantidadeEstoque, estoqueMinimo, estoqueMaximo);
    this.unidadeMedida = unidadeMedida;
    validateInsumo();
  }

  public void atualizar(
      String nome,
      String descricao,
      BigDecimal precoBase,
      boolean ativo,
      String unidadeMedida,
      Integer quantidadeEstoque,
      Integer estoqueMinimo,
      Integer estoqueMaximo) {
    super.atualizarDadosBasicos(nome, descricao);
    super.atualizarPreco(precoBase);
    super.atualizarEstoque(quantidadeEstoque, estoqueMinimo, estoqueMaximo);
    if (ativo) {
      super.ativar();
    } else {
      super.inativar();
    }
    this.unidadeMedida = unidadeMedida;
    validateInsumo();
  }

  public void atualizar(Insumo novosDados) {
    this.atualizar(
        novosDados.getNome(),
        novosDados.getDescricao(),
        novosDados.getPrecoBase(),
        novosDados.isAtivo(),
        novosDados.getUnidadeMedida(),
        novosDados.getQuantidadeEstoque(),
        novosDados.getEstoqueMinimo(),
        novosDados.getEstoqueMaximo());
  }

  private void validateInsumo() {
    if (unidadeMedida == null || unidadeMedida.isBlank()) {
      throw new IllegalArgumentException("Unidade de medida é obrigatória para insumos");
    }
  }
}
