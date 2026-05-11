package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.AtualizarParametrosEstoqueRequest;
import com.fiap.mecanica.presentation.dto.BaixaEstoqueRequest;
import com.fiap.mecanica.presentation.dto.EntradaEstoqueRequest;
import com.fiap.mecanica.presentation.dto.InsumoResponse;
import com.fiap.mecanica.presentation.dto.PecaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Estoque", description = "Gerenciamento de estoque de peças e insumos")
@SecurityRequirement(name = "bearerAuth")
public interface EstoqueApi {

  @Operation(
      summary = "Realizar baixa de estoque",
      description = "Deduz a quantidade do estoque de uma peça ou insumo.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Baixa realizada com sucesso",
            content =
                @Content(schema = @Schema(oneOf = {PecaResponse.class, InsumoResponse.class}))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "422",
            description = "Estoque insuficiente para a baixa solicitada"),
        @ApiResponse(responseCode = "404", description = "Item não encontrado")
      })
  ResponseEntity<?> baixarEstoque(BaixaEstoqueRequest request);

  @Operation(
      summary = "Registrar entrada de estoque",
      description = "Adiciona quantidade ao estoque de uma peça ou insumo.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Entrada registrada com sucesso",
            content =
                @Content(schema = @Schema(oneOf = {PecaResponse.class, InsumoResponse.class}))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Item não encontrado")
      })
  ResponseEntity<?> registrarEntradaEstoque(EntradaEstoqueRequest request);

  @Operation(
      summary = "Atualizar parâmetros de estoque",
      description = "Atualiza estoque mínimo e máximo de uma peça ou insumo.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Parâmetros de estoque atualizados com sucesso",
            content =
                @Content(schema = @Schema(oneOf = {PecaResponse.class, InsumoResponse.class}))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Item não encontrado")
      })
  ResponseEntity<?> atualizarParametrosEstoque(AtualizarParametrosEstoqueRequest request);
}
