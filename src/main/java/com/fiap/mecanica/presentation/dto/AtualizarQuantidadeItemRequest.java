package com.fiap.mecanica.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarQuantidadeItemRequest {

  @Schema(description = "Nova quantidade do item", example = "2")
  @NotNull(message = "A quantidade é obrigatória") @Min(value = 1, message = "A quantidade deve ser maior que zero")
  private Integer quantidade;
}
