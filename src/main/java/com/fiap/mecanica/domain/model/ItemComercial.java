package com.fiap.mecanica.domain.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ItemComercial {

  @EqualsAndHashCode.Include private UUID id;
  private String nome;
  private String descricao;
  private BigDecimal precoBase;
  private boolean ativo;

  public ItemComercial(
      UUID id, String nome, String descricao, BigDecimal precoBase, boolean ativo) {
    this.id = id;
    this.nome = nome;
    this.descricao = descricao;
    this.precoBase = precoBase;
    this.ativo = ativo;
    validate();
  }

  protected final void validate() {
    if (nome == null || nome.isBlank()) {
      throw new IllegalArgumentException("Nome do item é obrigatório");
    }
    if (descricao == null || descricao.isBlank()) {
      throw new IllegalArgumentException("Descrição do item é obrigatória");
    }
    if (precoBase == null || precoBase.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Preço base não pode ser negativo");
    }
  }

  public void atualizarPreco(BigDecimal novoPreco) {
    if (novoPreco == null || novoPreco.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Novo preço inválido");
    }
    this.precoBase = novoPreco;
  }

  public void atualizarDadosBasicos(String nome, String descricao) {
    if (nome == null || nome.isBlank()) {
      throw new IllegalArgumentException("Nome do item é obrigatório");
    }
    this.nome = nome;
    this.descricao = descricao;
  }

  public void ativar() {
    this.ativo = true;
  }

  public void inativar() {
    this.ativo = false;
  }
}
