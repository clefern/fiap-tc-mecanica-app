package com.fiap.mecanica.infra.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "veiculos", schema = "public")
public class VeiculoEntity {

  @Id
  @GeneratedValue
  @EqualsAndHashCode.Include
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cliente_id", nullable = false)
  private ClienteEntity cliente;

  @Column(name = "placa", nullable = false, unique = true)
  private String placa;

  @Column(name = "marca", nullable = false)
  private String marca;

  @Column(name = "modelo", nullable = false)
  private String modelo;

  @Column(name = "ano", nullable = false)
  private int ano;
}
