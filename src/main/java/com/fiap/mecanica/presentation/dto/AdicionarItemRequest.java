package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.TipoItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdicionarItemRequest {

  @NotNull(message = "O tipo do item é obrigatório") private TipoItem tipo;

  @NotBlank(message = "A descrição do item é obrigatória")
  private String descricao;

  @NotNull(message = "O valor unitário é obrigatório") @DecimalMin(value = "0.01", message = "O valor unitário deve ser maior que zero")
  private BigDecimal valorUnitario;

  @NotNull(message = "A quantidade é obrigatória") @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
  private Integer quantidade;

  @NotNull(message = "O ID de referência é obrigatório") private UUID referenciaId;
}
