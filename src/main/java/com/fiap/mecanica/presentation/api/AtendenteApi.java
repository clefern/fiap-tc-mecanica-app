package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.AtendenteRequest;
import com.fiap.mecanica.presentation.dto.AtendenteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Atendentes", description = "Gestão de atendentes da oficina")
@SecurityRequirement(name = "bearerAuth")
public interface AtendenteApi {

  @Operation(
      summary = "Cadastrar novo atendente",
      description = "Registra um novo atendente no sistema. Requer perfil ADMIN.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Atendente criado com sucesso",
            content = @Content(schema = @Schema(implementation = AtendenteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Requer perfil ADMIN"),
        @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
      })
  ResponseEntity<AtendenteResponse> create(
      @Parameter(description = "Dados do atendente", required = true)
          @RequestBody(description = "Payload de criação")
          @Valid
          AtendenteRequest request);

  @Operation(
      summary = "Buscar atendente por ID",
      description = "Retorna os detalhes de um atendente pelo seu UUID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Atendente encontrado",
            content = @Content(schema = @Schema(implementation = AtendenteResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Atendente não encontrado")
      })
  ResponseEntity<AtendenteResponse> getById(
      @Parameter(description = "UUID do atendente", required = true) @PathVariable UUID id);

  @Operation(
      summary = "Buscar atendente por CPF",
      description = "Busca um atendente utilizando seu CPF.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Atendente encontrado",
            content = @Content(schema = @Schema(implementation = AtendenteResponse.class))),
        @ApiResponse(responseCode = "400", description = "CPF inválido"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Atendente não encontrado")
      })
  ResponseEntity<AtendenteResponse> getByCpf(
      @Parameter(
              description = "CPF do atendente (apenas números)",
              required = true,
              example = "52998224725")
          @PathVariable
          String cpf);

  @Operation(
      summary = "Atualizar atendente",
      description = "Atualiza os dados de um atendente existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Atendente atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = AtendenteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Atendente não encontrado")
      })
  ResponseEntity<AtendenteResponse> update(
      @Parameter(description = "UUID do atendente", required = true) @PathVariable UUID id,
      @Parameter(description = "Dados atualizados", required = true) @RequestBody @Valid
          AtendenteRequest request);

  @Operation(
      summary = "Excluir atendente",
      description = "Remove um atendente do sistema.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Atendente excluído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Atendente não encontrado")
      })
  ResponseEntity<Void> delete(
      @Parameter(description = "UUID do atendente", required = true) @PathVariable UUID id);

  @Operation(
      summary = "Listar atendentes",
      description = "Retorna uma lista paginada de todos os atendentes.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de atendentes retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<AtendenteResponse>> getAll(
      @Parameter(description = "Paginação") @PageableDefault(size = 10, sort = "nome")
          Pageable pageable);
}
