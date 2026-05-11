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
    name = "atendentes",
    indexes = {@Index(name = "idx_atendente_cpf", columnList = "cpf")})
@DiscriminatorValue("ATENDENTE")
@PrimaryKeyJoinColumn(name = "id")
public class AtendenteEntity extends UserEntity {

  @Column(nullable = false, unique = true)
  private String cpf;
}
