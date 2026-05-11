package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.presentation.dto.OrdemServicoResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Prioridade OS", description = "Gestão de filas e prioridade de Ordens de Serviço")
@SecurityRequirement(name = "bearerAuth")
public interface PrioridadeOsApi {

  @Operation(
      summary = "Listar fila de orçamento ordenada por prioridade",
      description = "Retorna OS no status AGUARDANDO_APROVACAO, ordenadas por prioridade e data.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Fila de orçamento retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<OrdemServicoResponse>> listarFilaOrcamento(Pageable pageable);

  @Operation(
      summary = "Listar fila de execução ordenada por prioridade",
      description = "Retorna OS no status APROVADA e EM_EXECUCAO, ordenadas por prioridade e data.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Fila de execução retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<OrdemServicoResponse>> listarFilaExecucao(Pageable pageable);

  @Operation(
      summary = "Obter a próxima OS prioritária",
      description =
          "Retorna a próxima OS a ser atendida na fila de orçamento ou execução, conforme o"
              + " tipo informado. Leva em conta prioridade e data de entrada.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Próxima OS retornada com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Nenhuma OS na fila")
      })
  ResponseEntity<OrdemServicoResponse> obterProxima(
      @Parameter(
              description = "Tipo da fila: ORCAMENTO (padrão) ou EXECUCAO",
              example = "ORCAMENTO")
          @RequestParam(defaultValue = "ORCAMENTO")
          String tipo);

  @Operation(
      summary = "Atualizar prioridade da OS",
      description =
          "Define a prioridade de uma OS (BAIXA, NORMAL, ALTA, URGENTE). Requer perfil ADMIN.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Prioridade atualizada com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Requer perfil ADMIN"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
        @ApiResponse(responseCode = "422", description = "Prioridade inválida")
      })
  ResponseEntity<OrdemServicoResponse> atualizarPrioridade(
      @PathVariable UUID id, @RequestBody Prioridade prioridade);
}
