package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.StatusEstoque;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de resposta de uma peça")
public class PecaResponse {

  @Schema(description = "ID único da peça")
  private UUID id;

  @Schema(description = "Nome da peça")
  private String nome;

  @Schema(description = "Descrição detalhada")
  private String descricao;

  @Schema(description = "Preço base")
  private BigDecimal precoBase;

  @Schema(description = "Fabricante")
  private String fabricante;

  @Schema(description = "Código do fabricante")
  private String codigoFabricante;

  @Schema(description = "Modelo")
  private String modelo;

  @Schema(description = "Status de atividade")
  private boolean ativo;

  @Schema(description = "Quantidade disponível em estoque")
  private Integer quantidadeEstoque;

  @Schema(description = "Quantidade mínima para alerta")
  private Integer estoqueMinimo;

  @Schema(description = "Quantidade máxima recomendada")
  private Integer estoqueMaximo;

  @Schema(description = "Status atual do estoque (NORMAL, PRE_ALERTA, CRITICO, RUPTURA)")
  private StatusEstoque statusEstoque;
}
