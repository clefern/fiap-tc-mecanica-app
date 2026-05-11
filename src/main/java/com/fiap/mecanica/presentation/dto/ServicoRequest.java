package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação ou atualização de um serviço")
public class ServicoRequest {

  @Schema(description = "Nome do serviço", example = "Troca de Óleo")
  @NotBlank(message = "Nome é obrigatório")
  private String nome;

  @Schema(
      description = "Descrição detalhada do serviço",
      example = "Troca de óleo completa com filtro")
  @NotBlank(message = "Descrição é obrigatória")
  private String descricao;

  @Schema(description = "Valor base do serviço", example = "150.00")
  @NotNull(message = "Valor base é obrigatório") @PositiveOrZero(message = "Valor base deve ser positivo ou zero")
  private BigDecimal valorBase;

  @Schema(description = "Tempo estimado em minutos", example = "45")
  @NotNull(message = "Tempo estimado é obrigatório") @Min(value = 1, message = "Tempo estimado deve ser de pelo menos 1 minuto")
  private Long tempoEstimadoMinutos;

  @Schema(description = "Categoria do serviço", example = "MANUTENCAO_BASICA")
  @NotNull(message = "Categoria é obrigatória") private CategoriaServico categoria;

  @Schema(description = "Indica se o serviço está ativo", example = "true", defaultValue = "true")
  private boolean ativo = true;
}
