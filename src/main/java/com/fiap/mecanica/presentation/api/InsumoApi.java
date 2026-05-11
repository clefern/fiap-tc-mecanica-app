package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.InsumoRequest;
import com.fiap.mecanica.presentation.dto.InsumoResponse;
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

@Tag(name = "Insumos", description = "Gestão do catálogo de insumos da oficina")
@SecurityRequirement(name = "bearerAuth")
public interface InsumoApi {

  @Operation(
      summary = "Cadastrar novo insumo",
      description = "Adiciona um novo insumo ao catálogo da oficina.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Insumo criado com sucesso",
            content = @Content(schema = @Schema(implementation = InsumoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para cadastrar insumos")
      })
  ResponseEntity<InsumoResponse> create(
      @Parameter(description = "Dados do novo insumo", required = true)
          @RequestBody(description = "Payload de criação")
          @Valid
          InsumoRequest request);

  @Operation(
      summary = "Listar todos os insumos",
      description =
          "Retorna uma lista paginada de todos os insumos cadastrados, incluindo inativos."
              + " Resposta cacheada no servidor.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de insumos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<InsumoResponse>> getAll(
      @Parameter(description = "Paginação") @PageableDefault(size = 10, sort = "nome")
          Pageable pageable);

  @Operation(
      summary = "Pesquisar insumos por termo",
      description =
          "Busca insumos filtrando por nome ou descrição contendo o termo informado."
              + " Apenas insumos ativos são retornados.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de insumos filtrados retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<InsumoResponse>> search(
      @Parameter(description = "Termo de busca aplicado em nome e descrição", required = true)
          @RequestParam
          String termo,
      @Parameter(hidden = true) @PageableDefault(size = 10, sort = "nome") Pageable pageable);

  @Operation(
      summary = "Listar apenas insumos ativos",
      description = "Retorna uma lista paginada contendo apenas os insumos marcados como ativos.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de insumos ativos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<InsumoResponse>> getAllAtivos(
      @Parameter(hidden = true) @PageableDefault(size = 10, sort = "nome") Pageable pageable);

  @Operation(
      summary = "Buscar insumo por ID",
      description = "Busca os detalhes de um insumo específico pelo seu ID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Insumo encontrado",
            content = @Content(schema = @Schema(implementation = InsumoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Insumo não encontrado")
      })
  ResponseEntity<InsumoResponse> getById(
      @Parameter(description = "ID do insumo", required = true) @PathVariable UUID id);

  @Operation(
      summary = "Atualizar insumo",
      description = "Atualiza os dados de um insumo existente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Insumo atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = InsumoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Insumo não encontrado")
      })
  ResponseEntity<InsumoResponse> update(
      @Parameter(description = "ID do insumo", required = true) @PathVariable UUID id,
      @Parameter(description = "Dados atualizados", required = true) @RequestBody @Valid
          InsumoRequest request);

  @Operation(
      summary = "Excluir insumo",
      description = "Remove um insumo do catálogo.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Insumo excluído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Insumo não encontrado")
      })
  ResponseEntity<Void> delete(
      @Parameter(description = "ID do insumo", required = true) @PathVariable UUID id);
}
