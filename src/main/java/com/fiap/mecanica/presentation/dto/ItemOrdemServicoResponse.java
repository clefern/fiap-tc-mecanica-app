package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.TipoItem;
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
public class ItemOrdemServicoResponse {
  private UUID id;
  private TipoItem tipo;
  private String descricao;
  private BigDecimal valorUnitario;
  private Integer quantidade;
  private BigDecimal subtotal;
  private UUID referenciaId;
}
