package com.fiap.mecanica.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "insumos")
@DiscriminatorValue("INSUMO")
@PrimaryKeyJoinColumn(name = "id")
public class InsumoEntity extends ItemComercialEntity {

  @Column(name = "unidade_medida", nullable = false)
  private String unidadeMedida;

  @Column(name = "quantidade_estoque")
  private Integer quantidadeEstoque = 0;

  @Column(name = "estoque_minimo")
  private Integer estoqueMinimo = 0;

  @Column(name = "estoque_maximo")
  private Integer estoqueMaximo = 0;
}
