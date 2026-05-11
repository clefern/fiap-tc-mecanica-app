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
@Schema(description = "Dados para criação ou atualização de uma peça")
public class PecaRequest {

  @Schema(description = "Nome da peça", example = "Pastilha de Freio")
  @NotBlank(message = "Nome é obrigatório")
  private String nome;

  @Schema(description = "Descrição detalhada", example = "Pastilha de freio dianteira cerâmica")
  @NotBlank(message = "Descrição é obrigatória")
  private String descricao;

  @Schema(description = "Preço base de venda", example = "250.00")
  @NotNull(message = "Preço base é obrigatório") @PositiveOrZero(message = "Preço base deve ser positivo ou zero")
  private BigDecimal precoBase;

  @Schema(description = "Fabricante da peça", example = "Bosch")
  @NotBlank(message = "Fabricante é obrigatório")
  private String fabricante;

  @Schema(description = "Código do fabricante (Part Number)", example = "0986424750")
  @NotBlank(message = "Código do fabricante é obrigatório")
  private String codigoFabricante;

  @Schema(description = "Modelo compatível ou linha", example = "Golf/Jetta 2015+")
  private String modelo;

  @Schema(
      description = "Indica se a peça está ativa para venda",
      example = "true",
      defaultValue = "true")
  private boolean ativo = true;

  @Schema(description = "Quantidade disponível em estoque", example = "50")
  @PositiveOrZero(message = "Quantidade em estoque não pode ser negativa")
  private Integer quantidadeEstoque = 0;

  @Schema(description = "Quantidade mínima para alerta de reposição", example = "10")
  @PositiveOrZero(message = "Estoque mínimo não pode ser negativo")
  private Integer estoqueMinimo = 0;

  @Schema(description = "Quantidade máxima para cálculo de reposição", example = "100")
  @PositiveOrZero(message = "Estoque máximo não pode ser negativo")
  private Integer estoqueMaximo = 0;
}
