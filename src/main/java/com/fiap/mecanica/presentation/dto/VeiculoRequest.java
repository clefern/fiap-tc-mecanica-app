package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.presentation.validation.PlacaBrasil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Payload para criação/atualização de Veículo")
public class VeiculoRequest {
  @Schema(example = "ABC1234")
  @NotBlank
  @PlacaBrasil
  private String placa;

  @Schema(example = "Toyota")
  @NotBlank
  private String marca;

  @Schema(example = "Corolla")
  @NotBlank
  private String modelo;

  @Schema(example = "2020")
  @NotNull @Min(1900)
  private Integer ano;
}
