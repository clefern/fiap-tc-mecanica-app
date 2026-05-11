package com.fiap.mecanica.domain.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class Peca extends ItemEstocavel {

  private String fabricante;
  private String codigoFabricante;
  private String modelo;

  public Peca(
      UUID id,
      String nome,
      String descricao,
      BigDecimal precoBase,
      boolean ativo,
      String fabricante,
      String codigoFabricante,
      String modelo,
      Integer quantidadeEstoque,
      Integer estoqueMinimo,
      Integer estoqueMaximo) {
    super(id, nome, descricao, precoBase, ativo, quantidadeEstoque, estoqueMinimo, estoqueMaximo);
    this.fabricante = fabricante;
    this.codigoFabricante = codigoFabricante;
    this.modelo = modelo;
  }

  public void atualizar(
      String nome,
      String descricao,
      BigDecimal precoBase,
      boolean ativo,
      String fabricante,
      String codigoFabricante,
      String modelo,
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
    this.fabricante = fabricante;
    this.codigoFabricante = codigoFabricante;
    this.modelo = modelo;
  }

  public void atualizar(Peca novosDados) {
    this.atualizar(
        novosDados.getNome(),
        novosDados.getDescricao(),
        novosDados.getPrecoBase(),
        novosDados.isAtivo(),
        novosDados.getFabricante(),
        novosDados.getCodigoFabricante(),
        novosDados.getModelo(),
        novosDados.getQuantidadeEstoque(),
        novosDados.getEstoqueMinimo(),
        novosDados.getEstoqueMaximo());
  }
}
