package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.PecaRequest;
import com.fiap.mecanica.presentation.dto.PecaResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Peças", description = "Gestão do catálogo de peças da oficina")
@SecurityRequirement(name = "bearerAuth")
public interface PecaApi {

  @Operation(
      summary = "Cadastrar nova peça",
      description = "Adiciona uma nova peça ao catálogo da oficina.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Peça criada com sucesso",
            content = @Content(schema = @Schema(implementation = PecaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para cadastrar peças")
      })
  ResponseEntity<PecaResponse> create(
      @Parameter(description = "Dados da nova peça", required = true)
          @RequestBody(description = "Payload de criação")
          @Valid
          PecaRequest request);

  @Operation(
      summary = "Listar todas as peças",
      description =
          "Retorna uma lista paginada de todas as peças cadastradas, incluindo inativas."
              + " Resposta cacheada no servidor.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de peças retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<PecaResponse>> getAll(
      @Parameter(description = "Paginação") @PageableDefault(size = 10, sort = "nome")
          Pageable pageable);

  @Operation(
      summary = "Pesquisar peças por termo",
      description =
          "Busca peças filtrando por nome ou descrição contendo o termo informado."
              + " Apenas peças ativas são retornadas.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de peças filtradas retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<PecaResponse>> search(
      @Parameter(description = "Termo de busca aplicado em nome e descrição", required = true)
          @RequestParam
          String termo,
      @Parameter(hidden = true) @PageableDefault(size = 10, sort = "nome") Pageable pageable);

  @Operation(
      summary = "Listar apenas peças ativas",
      description = "Retorna uma lista paginada contendo apenas as peças marcadas como ativas.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de peças ativas retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<PecaResponse>> getAllAtivos(
      @Parameter(hidden = true) @PageableDefault(size = 10, sort = "nome") Pageable pageable);

  @Operation(
      summary = "Buscar peça por ID",
      description = "Busca os detalhes de uma peça específica pelo seu ID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Peça encontrada",
            content = @Content(schema = @Schema(implementation = PecaResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Peça não encontrada")
      })
  ResponseEntity<PecaResponse> getById(
      @Parameter(description = "ID da peça", required = true) @PathVariable UUID id);

  @Operation(
      summary = "Atualizar peça",
      description = "Atualiza os dados de uma peça existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Peça atualizada com sucesso",
            content = @Content(schema = @Schema(implementation = PecaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Peça não encontrada")
      })
  ResponseEntity<PecaResponse> update(
      @Parameter(description = "ID da peça", required = true) @PathVariable UUID id,
      @Parameter(description = "Dados atualizados", required = true) @RequestBody @Valid
          PecaRequest request);

  @Operation(
      summary = "Excluir peça",
      description = "Remove uma peça do catálogo.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Peça excluída com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Peça não encontrada")
      })
  ResponseEntity<Void> delete(
      @Parameter(description = "ID da peça", required = true) @PathVariable UUID id);
}
