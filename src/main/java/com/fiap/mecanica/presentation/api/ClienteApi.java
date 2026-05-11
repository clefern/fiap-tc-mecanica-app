package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.ClienteRequest;
import com.fiap.mecanica.presentation.dto.ClienteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Clientes", description = "Gestão de clientes da oficina")
@SecurityRequirement(name = "bearerAuth")
public interface ClienteApi {

  @Operation(
      summary = "Cadastrar novo cliente",
      description = "Cadastra um novo cliente (Pessoa Física ou Jurídica) no sistema.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Cliente criado com sucesso",
            content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "409", description = "CPF ou CNPJ já cadastrado")
      })
  ResponseEntity<ClienteResponse> create(
      @Parameter(description = "Dados do cliente", required = true) ClienteRequest request);

  @Operation(
      summary = "Buscar cliente por ID",
      description = "Busca os detalhes de um cliente pelo seu UUID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Cliente encontrado",
            content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  ResponseEntity<ClienteResponse> getById(
      @Parameter(description = "UUID do cliente", required = true) UUID id);

  @Operation(
      summary = "Buscar cliente por documento",
      description = "Busca um cliente pelo CPF ou CNPJ.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Cliente encontrado",
            content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Documento inválido"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  ResponseEntity<ClienteResponse> getByDocumento(
      @Parameter(
              description = "Número do documento (CPF/CNPJ, apenas números)",
              required = true,
              example = "39053344705")
          String documento);

  @Operation(
      summary = "Listar todos os clientes",
      description = "Retorna uma lista paginada de todos os clientes cadastrados.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clientes",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<ClienteResponse>> getAll(Pageable pageable);

  @Operation(
      summary = "Atualizar cliente",
      description = "Atualiza os dados de um cliente existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Cliente atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "409", description = "Documento já cadastrado em outro cliente")
      })
  ResponseEntity<ClienteResponse> update(
      @Parameter(description = "UUID do cliente", required = true) UUID id,
      @Parameter(description = "Dados atualizados do cliente", required = true)
          ClienteRequest request);

  @Operation(
      summary = "Remover cliente",
      description = "Remove um cliente do sistema.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  ResponseEntity<Void> delete(@Parameter(description = "UUID do cliente", required = true) UUID id);
}
