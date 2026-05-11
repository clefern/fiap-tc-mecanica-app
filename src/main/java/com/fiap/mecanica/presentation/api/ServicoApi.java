package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.ServicoRequest;
import com.fiap.mecanica.presentation.dto.ServicoResponse;
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

@Tag(name = "Serviços", description = "Gestão do catálogo de serviços da oficina")
@SecurityRequirement(name = "bearerAuth")
public interface ServicoApi {

  @Operation(
      summary = "Cadastrar novo serviço",
      description = "Adiciona um novo serviço ao catálogo da oficina.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Serviço criado com sucesso",
            content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para cadastrar serviços")
      })
  ResponseEntity<ServicoResponse> create(
      @Parameter(description = "Dados do novo serviço", required = true)
          @RequestBody(description = "Payload de criação")
          @Valid
          ServicoRequest request);

  @Operation(
      summary = "Listar todos os serviços",
      description =
          "Retorna uma lista paginada de todos os serviços cadastrados, incluindo inativos."
              + " Resposta cacheada no servidor.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de serviços retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<ServicoResponse>> getAll(
      @Parameter(description = "Paginação") @PageableDefault(size = 10, sort = "descricao")
          Pageable pageable);

  @Operation(
      summary = "Listar apenas serviços ativos",
      description = "Retorna uma lista paginada contendo apenas os serviços marcados como ativos.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de serviços ativos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<ServicoResponse>> getAllAtivos(
      @Parameter(hidden = true) @PageableDefault(size = 10, sort = "descricao") Pageable pageable);

  @Operation(
      summary = "Buscar serviço por ID",
      description = "Busca os detalhes de um serviço específico pelo seu ID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Serviço encontrado",
            content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
      })
  ResponseEntity<ServicoResponse> getById(
      @Parameter(description = "ID do serviço", required = true) @PathVariable UUID id);

  @Operation(
      summary = "Atualizar serviço",
      description = "Atualiza os dados de um serviço existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Serviço atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = ServicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
      })
  ResponseEntity<ServicoResponse> update(
      @Parameter(description = "ID do serviço", required = true) @PathVariable UUID id,
      @Parameter(description = "Dados atualizados", required = true) @RequestBody @Valid
          ServicoRequest request);

  @Operation(
      summary = "Excluir serviço",
      description = "Remove um serviço do catálogo.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Serviço excluído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
      })
  ResponseEntity<Void> delete(
      @Parameter(description = "ID do serviço", required = true) @PathVariable UUID id);
}
