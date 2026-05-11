package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.TipoItem;
import java.io.Serial;
import java.io.Serializable;
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
public class ItemOrdemServico implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private UUID id;
  private TipoItem tipo;
  private String descricao;
  private BigDecimal valorUnitario;
  private Integer quantidade;

  // ID da referência externa (Serviço, Peça ou Insumo)
  private UUID referenciaId;

  public BigDecimal getSubtotal() {
    if (valorUnitario == null || quantidade == null) {
      return BigDecimal.ZERO;
    }
    return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
  }
}
