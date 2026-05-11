package com.fiap.mecanica.domain.model.relatorio;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioDesempenhoMecanico {
  private UUID mecanicoId;
  private String nomeMecanico;
  private Long quantidadeOsConcluidas;
  private BigDecimal receitaTotal;
  private Duration tempoMedioConclusao;
}
