package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
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
public class OrcamentoResponse {
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
}
