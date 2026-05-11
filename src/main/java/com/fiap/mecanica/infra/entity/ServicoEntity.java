package com.fiap.mecanica.infra.entity;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "servicos")
@DiscriminatorValue("SERVICO")
@PrimaryKeyJoinColumn(name = "id")
public class ServicoEntity extends ItemComercialEntity {

  @Column(name = "tempo_estimado_minutos")
  private Long tempoEstimadoMinutos;

  @Enumerated(EnumType.STRING)
  @Column(name = "categoria")
  private CategoriaServico categoria;
}
