package com.fiap.mecanica.infra.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "clientes",
    indexes = {@Index(name = "idx_cliente_documento", columnList = "documento")})
@DiscriminatorValue("CLIENTE")
@PrimaryKeyJoinColumn(name = "id")
public class ClienteEntity extends UserEntity {

  @Column(nullable = false, unique = true)
  private String documento;

  @Column(name = "tipo_pessoa", nullable = false)
  private String tipoPessoa;

  @Column(nullable = false)
  private String telefone;

  @Column(nullable = false)
  private String endereco;

  @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private List<VeiculoEntity> veiculos = new ArrayList<>();
}
