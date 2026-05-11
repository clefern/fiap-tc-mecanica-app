package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.OrcamentoResponse;
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

@Tag(name = "Orçamentos", description = "Gestão de orçamentos de serviços mecânicos")
@SecurityRequirement(name = "bearerAuth")
public interface OrcamentoApi {

  @Operation(
      summary = "Buscar orçamento por ID",
      description = "Retorna os detalhes de um orçamento específico",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orçamento encontrado",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
      })
  ResponseEntity<OrcamentoResponse> buscarPorId(
      @Parameter(description = "ID do orçamento", required = true) UUID id);

  @Operation(
      summary = "Buscar orçamento por código",
      description = "Retorna o orçamento pelo código legível (ex: #ORC-5329ABD0)",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orçamento encontrado",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
      })
  ResponseEntity<OrcamentoResponse> buscarPorCodigo(
      @Parameter(description = "Código do orçamento (ex: #ORC-5329ABD0)", required = true)
          String codigo);

  @Operation(
      summary = "Buscar orçamento por Ordem de Serviço",
      description = "Retorna o orçamento associado a uma OS",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orçamento encontrado",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Orçamento não encontrado para esta OS")
      })
  ResponseEntity<OrcamentoResponse> buscarPorOrdemServico(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID ordemServicoId);

  @Operation(
      summary = "Listar todos os orçamentos",
      description = "Retorna uma lista paginada de orçamentos",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<OrcamentoResponse>> listarTodos(Pageable pageable);

  @Operation(
      summary = "Deletar orçamento",
      description = "Remove um orçamento do sistema",
      responses = {
        @ApiResponse(responseCode = "204", description = "Orçamento deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
      })
  ResponseEntity<Void> deletar(
      @Parameter(description = "ID do orçamento", required = true) UUID id);

  @Operation(
      summary = "Aprovar orçamento",
      description = "Aprova o orçamento e dispara a transição da OS para APROVADA",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orçamento aprovado com sucesso",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Orçamento não encontrado"),
        @ApiResponse(
            responseCode = "422",
            description = "Transição de status inválida para o orçamento")
      })
  ResponseEntity<OrcamentoResponse> aprovar(
      @Parameter(description = "ID do orçamento", required = true) UUID id);

  @Operation(
      summary = "Reprovar orçamento",
      description = "Reprova o orçamento e dispara o cancelamento da OS associada",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orçamento reprovado com sucesso",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Orçamento não encontrado"),
        @ApiResponse(
            responseCode = "422",
            description = "Transição de status inválida para o orçamento")
      })
  ResponseEntity<OrcamentoResponse> reprovar(
      @Parameter(description = "ID do orçamento", required = true) UUID id);

  @Operation(
      summary = "Aprovar orçamento por OS",
      description =
          "Busca o orçamento no status GERADO da OS informada e o aprova. Atalho conveniente"
              + " para o fluxo padrão de aprovação.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orçamento aprovado com sucesso",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "404",
            description = "Nenhum orçamento GERADO encontrado para a OS"),
        @ApiResponse(
            responseCode = "422",
            description = "Transição de status inválida para o orçamento")
      })
  ResponseEntity<OrcamentoResponse> aprovarPorOsId(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID osId);

  @Operation(
      summary = "Reprovar orçamento por OS",
      description =
          "Busca o orçamento no status GERADO da OS informada e o reprova. Atalho conveniente"
              + " para o fluxo de reprovação.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orçamento reprovado com sucesso",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "404",
            description = "Nenhum orçamento GERADO encontrado para a OS"),
        @ApiResponse(
            responseCode = "422",
            description = "Transição de status inválida para o orçamento")
      })
  ResponseEntity<OrcamentoResponse> reprovarPorOsId(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID osId);
}
