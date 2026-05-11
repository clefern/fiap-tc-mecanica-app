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
public class AtendenteResponse {
  private UUID id;

  @Schema(example = "Ana Atendente")
  private String nome;

  @Schema(example = "39053344705")
  private String cpf;

  @Schema(example = "ana@oficina.com")
  private String email;

  private boolean ativo;
}
