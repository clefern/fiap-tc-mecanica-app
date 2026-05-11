package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.VeiculoRequest;
import com.fiap.mecanica.presentation.dto.VeiculoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Veículos", description = "Gerenciamento de veículos dos clientes")
@SecurityRequirement(name = "bearerAuth")
public interface VeiculoApi {

  @Operation(
      summary = "Cadastrar veículo para um cliente",
      description = "Registra um novo veículo associado ao cliente informado.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Veículo criado com sucesso",
            content = @Content(schema = @Schema(implementation = VeiculoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "409", description = "Placa já cadastrada")
      })
  ResponseEntity<VeiculoResponse> create(
      @Parameter(description = "ID do cliente proprietário", required = true) UUID clienteId,
      @Parameter(description = "Dados do veículo", required = true) VeiculoRequest request);

  @Operation(
      summary = "Buscar veículo por placa",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Veículo encontrado",
            content = @Content(schema = @Schema(implementation = VeiculoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
      })
  ResponseEntity<VeiculoResponse> getByPlaca(
      @Parameter(description = "Placa do veículo (ex: ABC1234 ou ABC1D23)", required = true)
          String placa);

  @Operation(
      summary = "Remover veículo por placa",
      responses = {
        @ApiResponse(responseCode = "204", description = "Veículo removido com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
      })
  ResponseEntity<Void> deleteByPlaca(
      @Parameter(description = "Placa do veículo", required = true) String placa);

  @Operation(
      summary = "Listar veículos de um cliente",
      description =
          "Retorna a lista de veículos associados ao cliente. Resposta cacheada no servidor.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de veículos",
            content = @Content(schema = @Schema(implementation = VeiculoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  ResponseEntity<List<VeiculoResponse>> listByCliente(
      @Parameter(description = "ID do cliente", required = true) UUID clienteId);
}
