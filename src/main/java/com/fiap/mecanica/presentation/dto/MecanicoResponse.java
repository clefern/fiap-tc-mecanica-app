package com.fiap.mecanica.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MecanicoResponse {
  private UUID id;

  @Schema(example = "Carlos Mecânico")
  private String nome;

  @Schema(example = "52998224725")
  private String cpf;

  @Schema(example = "carlos@oficina.com")
  private String email;

  @Schema(example = "Motor e Suspensão")
  private String especialidade;

  private boolean ativo;
}
