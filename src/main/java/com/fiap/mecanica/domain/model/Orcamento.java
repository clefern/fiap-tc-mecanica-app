package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orcamento implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private UUID id;
  private String codigo;
  private UUID ordemServicoId;
  private UUID mecanicoDiagnosticoId;
  private LocalDateTime dataEmissao;
  private LocalDateTime dataValidade;
  private BigDecimal valorTotalMateriais;
  private BigDecimal valorTotalMaoDeObra;
  private BigDecimal valorImpostos;
  private BigDecimal valorTotal;
  private StatusOrcamento status;
  private String urlPdf;

  public void aprovar() {
    if (this.status != StatusOrcamento.GERADO) {
      throw new IllegalStateException("Apenas orçamentos gerados podem ser aprovados.");
    }
    this.status = StatusOrcamento.APROVADO;
  }

  public void reprovar() {
    if (this.status != StatusOrcamento.GERADO) {
      throw new IllegalStateException("Apenas orçamentos gerados podem ser reprovados.");
    }
    this.status = StatusOrcamento.REJEITADO;
  }

  public void cancelar() {
    // Regra de negócio: Orçamentos podem ser cancelados se não estiverem já finalizados/pagos
    // (simplificado)
    this.status = StatusOrcamento.CANCELADO;
  }
}
