package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.TipoItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaixaEstoqueRequest {
  @NotNull private UUID referenciaId;
  @NotNull private TipoItem tipo;

  @NotNull @Min(1)
  private Integer quantidade;
}
