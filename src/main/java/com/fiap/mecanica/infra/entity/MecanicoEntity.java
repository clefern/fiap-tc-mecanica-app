package com.fiap.mecanica.infra.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "mecanicos",
    indexes = {@Index(name = "idx_mecanico_cpf", columnList = "cpf")})
@DiscriminatorValue("MECANICO")
@PrimaryKeyJoinColumn(name = "id")
public class MecanicoEntity extends UserEntity {

  @Column(nullable = false, unique = true)
  private String cpf;

  @Column(nullable = false)
  private String especialidade;
}
