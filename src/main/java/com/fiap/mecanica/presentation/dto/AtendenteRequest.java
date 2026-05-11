package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.presentation.validation.CpfValid;
import com.fiap.mecanica.presentation.validation.EmailNormalized;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Payload para criação/atualização de Atendente")
public class AtendenteRequest {
  @Schema(example = "Ana Atendente")
  @NotBlank
  private String nome;

  @Schema(example = "39053344705", description = "CPF (apenas números)")
  @NotBlank
  @CpfValid
  private String cpf;

  @Schema(example = "ana@oficina.com")
  @NotBlank
  @EmailNormalized
  private String email;

  @Schema(example = "MAT-123")
  @NotBlank
  private String matricula;
}
