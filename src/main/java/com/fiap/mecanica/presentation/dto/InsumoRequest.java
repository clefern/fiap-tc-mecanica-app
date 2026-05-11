package com.fiap.mecanica.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Dados para criação ou atualização de um insumo")
public class InsumoRequest {

  @Schema(description = "Nome do insumo", example = "Óleo Sintético 5W30")
  @NotBlank(message = "Nome é obrigatório")
  private String nome;

  @Schema(description = "Descrição detalhada", example = "Óleo de motor sintético alta performance")
  @NotBlank(message = "Descrição é obrigatória")
  private String descricao;

  @Schema(description = "Preço base por unidade", example = "45.00")
  @NotNull(message = "Preço base é obrigatório") @PositiveOrZero(message = "Preço base deve ser positivo ou zero")
  private BigDecimal precoBase;

  @Schema(description = "Unidade de medida", example = "LITRO")
  @NotBlank(message = "Unidade de medida é obrigatória")
  private String unidadeMedida;

  @Schema(description = "Indica se o insumo está ativo", example = "true", defaultValue = "true")
  private boolean ativo = true;

  @Schema(description = "Quantidade disponível em estoque", example = "100")
  @PositiveOrZero(message = "Quantidade em estoque não pode ser negativa")
  private Integer quantidadeEstoque = 0;

  @Schema(description = "Quantidade mínima para alerta de reposição", example = "20")
  @PositiveOrZero(message = "Estoque mínimo não pode ser negativo")
  private Integer estoqueMinimo = 0;

  @Schema(description = "Quantidade máxima para cálculo de reposição", example = "200")
  @PositiveOrZero(message = "Estoque máximo não pode ser negativo")
  private Integer estoqueMaximo = 0;
}
