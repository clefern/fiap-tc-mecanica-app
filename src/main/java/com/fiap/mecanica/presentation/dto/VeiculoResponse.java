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
public class VeiculoResponse {
  @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(example = "ABC1234")
  private String placa;

  @Schema(example = "Toyota")
  private String marca;

  @Schema(example = "Corolla")
  private String modelo;

  @Schema(example = "2020")
  private int ano;
}
