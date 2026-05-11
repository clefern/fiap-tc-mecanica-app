package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.TipoItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtualizarParametrosEstoqueRequest {

  @NotNull private UUID referenciaId;

  @NotNull private TipoItem tipo;

  @PositiveOrZero private Integer estoqueMinimo;

  @PositiveOrZero private Integer estoqueMaximo;
}
