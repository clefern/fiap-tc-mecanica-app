package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados retornados de um serviço")
public class ServicoResponse {
  @Schema(description = "ID único do serviço", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(description = "Nome do serviço", example = "Troca de Óleo")
  private String nome;

  @Schema(
      description = "Descrição detalhada do serviço",
      example = "Troca de óleo completa com filtro")
  private String descricao;

  @Schema(description = "Valor base do serviço", example = "150.00")
  private BigDecimal valorBase;

  @Schema(description = "Tempo estimado em minutos", example = "45")
  private Long tempoEstimadoMinutos;

  @Schema(description = "Categoria do serviço", example = "MANUTENCAO_BASICA")
  private CategoriaServico categoria;

  @Schema(description = "Status do serviço", example = "true")
  private boolean ativo;
}
