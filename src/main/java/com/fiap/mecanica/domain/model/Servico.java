package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class Servico extends ItemComercial {

  private Duration tempoEstimado;
  private CategoriaServico categoria;

  public Servico(
      UUID id,
      String nome,
      String descricao,
      BigDecimal precoBase,
      Duration tempoEstimado,
      CategoriaServico categoria) {
    super(id, nome, descricao, precoBase, true);
    this.tempoEstimado = tempoEstimado;
    this.categoria = categoria;
    validateServico();
  }

  // Constructor for creating new services (without ID) - leveraging parent's structure if needed,
  // but typically we pass null ID to parent or generate one.
  // The previous Servico class generated UUID in constructor. ItemComercial expects UUID.
  public Servico(
      String nome,
      String descricao,
      BigDecimal precoBase,
      Duration tempoEstimado,
      CategoriaServico categoria) {
    super(UUID.randomUUID(), nome, descricao, precoBase, true);
    this.tempoEstimado = tempoEstimado;
    this.categoria = categoria;
    validateServico();
  }

  // Constructor for full reconstruction (e.g. from persistence)
  public Servico(
      UUID id,
      String nome,
      String descricao,
      BigDecimal precoBase,
      boolean ativo,
      Duration tempoEstimado,
      CategoriaServico categoria) {
    super(id, nome, descricao, precoBase, ativo);
    this.tempoEstimado = tempoEstimado;
    this.categoria = categoria;
    validateServico();
  }

  private void validateServico() {
    if (tempoEstimado == null || tempoEstimado.isNegative() || tempoEstimado.isZero()) {
      throw new IllegalArgumentException("Tempo estimado deve ser positivo");
    }
    if (categoria == null) {
      throw new IllegalArgumentException("Categoria é obrigatória");
    }
  }

  public void atualizar(
      String nome,
      String descricao,
      BigDecimal precoBase,
      Duration tempoEstimado,
      CategoriaServico categoria,
      boolean ativo) {
    super.atualizarDadosBasicos(nome, descricao);
    super.atualizarPreco(precoBase);
    if (ativo) {
      super.ativar();
    } else {
      super.inativar();
    }

    this.tempoEstimado = tempoEstimado;
    this.categoria = categoria;
    validateServico();
  }
}
