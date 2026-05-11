package com.fiap.mecanica.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pecas")
@DiscriminatorValue("PECA")
@PrimaryKeyJoinColumn(name = "id")
public class PecaEntity extends ItemComercialEntity {

  @Column(name = "fabricante")
  private String fabricante;

  @Column(name = "codigo_fabricante")
  private String codigoFabricante;

  @Column(name = "modelo")
  private String modelo;

  @Column(name = "quantidade_estoque")
  private Integer quantidadeEstoque = 0;

  @Column(name = "estoque_minimo")
  private Integer estoqueMinimo = 0;

  @Column(name = "estoque_maximo")
  private Integer estoqueMaximo = 0;
}
