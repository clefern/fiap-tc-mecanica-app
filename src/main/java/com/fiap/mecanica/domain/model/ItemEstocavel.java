package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.StatusEstoque;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class ItemEstocavel extends ItemComercial {

  private Integer quantidadeEstoque;
  private Integer estoqueMinimo;
  private Integer estoqueMaximo;

  public ItemEstocavel(
      UUID id,
      String nome,
      String descricao,
      BigDecimal precoBase,
      boolean ativo,
      Integer quantidadeEstoque,
      Integer estoqueMinimo,
      Integer estoqueMaximo) {
    super(id, nome, descricao, precoBase, ativo);
    this.quantidadeEstoque = quantidadeEstoque != null ? quantidadeEstoque : 0;
    this.estoqueMinimo = estoqueMinimo != null ? estoqueMinimo : 0;
    this.estoqueMaximo = estoqueMaximo != null ? estoqueMaximo : 0;
    validateEstoque();
  }

  protected final void validateEstoque() {
    if (quantidadeEstoque < 0) {
      throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa");
    }
    if (estoqueMinimo < 0) {
      throw new IllegalArgumentException("Estoque mínimo não pode ser negativo");
    }
    if (estoqueMaximo < 0) {
      throw new IllegalArgumentException("Estoque máximo não pode ser negativo");
    }
    if (estoqueMaximo > 0 && estoqueMinimo > estoqueMaximo) {
      throw new IllegalArgumentException("Estoque mínimo não pode ser maior que o estoque máximo");
    }
  }

  public void atualizarEstoque(Integer quantidade, Integer estoqueMinimo, Integer estoqueMaximo) {
    if (quantidade != null) {
      if (quantidade < 0) {
        throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa");
      }
      this.quantidadeEstoque = quantidade;
    }
    if (estoqueMinimo != null) {
      if (estoqueMinimo < 0) {
        throw new IllegalArgumentException("Estoque mínimo não pode ser negativo");
      }
      this.estoqueMinimo = estoqueMinimo;
    }
    if (estoqueMaximo != null) {
      if (estoqueMaximo < 0) {
        throw new IllegalArgumentException("Estoque máximo não pode ser negativo");
      }
      this.estoqueMaximo = estoqueMaximo;
    }

    // Validação de consistência
    if (this.estoqueMaximo > 0 && this.estoqueMinimo > this.estoqueMaximo) {
      throw new IllegalArgumentException("Estoque mínimo não pode ser maior que o estoque máximo");
    }
  }

  public void adicionarEstoque(int quantidade) {
    if (quantidade <= 0) {
      throw new IllegalArgumentException("Quantidade para adicionar deve ser positiva");
    }
    this.quantidadeEstoque += quantidade;
  }

  public void baixarEstoque(int quantidade) {
    if (quantidade <= 0) {
      throw new IllegalArgumentException("Quantidade para baixar deve ser positiva");
    }
    if (this.quantidadeEstoque < quantidade) {
      throw new IllegalStateException(
          "Estoque insuficiente. Disponível: "
              + this.quantidadeEstoque
              + ", Solicitado: "
              + quantidade);
    }
    this.quantidadeEstoque -= quantidade;
  }

  public StatusEstoque verificarStatusEstoque() {
    if (this.quantidadeEstoque == 0) {
      return StatusEstoque.RUPTURA;
    }
    if (this.quantidadeEstoque <= this.estoqueMinimo) {
      return StatusEstoque.CRITICO;
    }

    // Pré-alerta: qtd <= min + 10% (max - min)
    if (this.estoqueMaximo > 0 && this.estoqueMaximo > this.estoqueMinimo) {
      double margem = (this.estoqueMaximo - this.estoqueMinimo) * 0.10;
      if (this.quantidadeEstoque <= (this.estoqueMinimo + margem)) {
        return StatusEstoque.PRE_ALERTA;
      }
    }

    return StatusEstoque.NORMAL;
  }

  public void aplicarPorTipo(Consumer<Peca> quandoPeca, Consumer<Insumo> quandoInsumo) {
    if (this instanceof Peca peca) {
      quandoPeca.accept(peca);
    } else if (this instanceof Insumo insumo) {
      quandoInsumo.accept(insumo);
    }
  }
}
