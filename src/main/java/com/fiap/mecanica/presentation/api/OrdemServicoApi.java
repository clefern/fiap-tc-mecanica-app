package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.presentation.dto.AberturaOsCompletaRequest;
import com.fiap.mecanica.presentation.dto.AdicionarItemRequest;
import com.fiap.mecanica.presentation.dto.AtualizarQuantidadeItemRequest;
import com.fiap.mecanica.presentation.dto.OrdemServicoRequest;
import com.fiap.mecanica.presentation.dto.OrdemServicoResponse;
import com.fiap.mecanica.presentation.dto.StatusOsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
    name = "Ordens de Serviço",
    description = "Criação, consulta e gestão de itens de Ordens de Serviço")
@SecurityRequirement(name = "bearerAuth")
public interface OrdemServicoApi {

  @Operation(
      summary = "Abrir OS completa (com serviços, insumos e peças)",
      description =
          "Cria uma nova Ordem de Serviço e adiciona itens (serviços, peças e insumos) em uma"
              + " única requisição atômica. A lista de itens é opcional.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "OS criada com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para abrir OS"),
        @ApiResponse(responseCode = "404", description = "Cliente ou Veículo não encontrado"),
        @ApiResponse(
            responseCode = "422",
            description = "Estoque insuficiente ou regra de domínio violada")
      })
  ResponseEntity<OrdemServicoResponse> abrirOsCompleta(
      @Parameter(description = "Dados para abertura completa da OS", required = true)
          @Valid
          @RequestBody
          AberturaOsCompletaRequest request);

  @Operation(
      summary = "Criar nova Ordem de Serviço",
      description =
          "Cria uma OS sem itens. Use `abrirOsCompleta` para criar com itens em uma única chamada.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Ordem de Serviço criada com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para criar OS"),
        @ApiResponse(responseCode = "404", description = "Cliente ou Veículo não encontrado")
      })
  ResponseEntity<OrdemServicoResponse> criar(
      @Parameter(description = "Dados para criação da OS", required = true) @Valid @RequestBody
          OrdemServicoRequest request);

  @Operation(
      summary = "Adicionar item à OS",
      description =
          "Adiciona um único item (peça, insumo ou serviço) à OS. Valida estoque para peças e"
              + " insumos.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Item adicionado com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados do item inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "404",
            description = "Ordem de Serviço ou Mecânico não encontrado"),
        @ApiResponse(responseCode = "422", description = "Estoque insuficiente")
      })
  ResponseEntity<OrdemServicoResponse> adicionarItem(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID id,
      @Parameter(description = "Dados do item", required = true) @Valid @RequestBody
          AdicionarItemRequest request);

  @Operation(
      summary = "Adicionar itens à OS em lote",
      description =
          "Adiciona múltiplos itens de uma vez. Falha atomicamente se algum estoque for"
              + " insuficiente.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Itens adicionados com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados dos itens inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "404",
            description = "Ordem de Serviço ou Mecânico não encontrado"),
        @ApiResponse(responseCode = "422", description = "Estoque insuficiente em um ou mais itens")
      })
  ResponseEntity<OrdemServicoResponse> adicionarItensEmLote(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID id,
      @Parameter(description = "Lista de itens", required = true) @Valid @RequestBody
          List<AdicionarItemRequest> requests);

  @Operation(
      summary = "Atualizar quantidade de item da OS",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Quantidade atualizada com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço ou Item não encontrado"),
        @ApiResponse(
            responseCode = "422",
            description = "Estoque insuficiente para nova quantidade")
      })
  ResponseEntity<OrdemServicoResponse> atualizarQuantidadeItem(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID id,
      @Parameter(description = "ID do Item", required = true) UUID itemId,
      @Parameter(description = "Nova quantidade", required = true) @Valid @RequestBody
          AtualizarQuantidadeItemRequest request);

  @Operation(
      summary = "Remover item da OS",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Item removido com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço ou Item não encontrado")
      })
  ResponseEntity<OrdemServicoResponse> removerItem(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID id,
      @Parameter(description = "ID do Item", required = true) UUID itemId);

  @Operation(
      summary = "Buscar OS por ID",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Ordem de Serviço encontrada",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada")
      })
  ResponseEntity<OrdemServicoResponse> buscarPorId(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID id);

  @Operation(
      summary = "Buscar OS por código",
      description = "Retorna a Ordem de Serviço pelo código legível (ex: #OS-5329ABD0)",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Ordem de Serviço encontrada",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada")
      })
  ResponseEntity<OrdemServicoResponse> buscarPorCodigo(
      @Parameter(description = "Código da Ordem de Serviço (ex: #OS-5329ABD0)", required = true)
          String codigo);

  @Operation(
      summary = "Consultar status da OS",
      description =
          "Retorna apenas o status atual e timestamps relevantes da OS, sem dados de itens."
              + " Adequado para polling leve de status.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Status da OS retornado com sucesso",
            content = @Content(schema = @Schema(implementation = StatusOsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada")
      })
  ResponseEntity<StatusOsResponse> buscarStatus(
      @Parameter(description = "ID da Ordem de Serviço", required = true) UUID id);

  @Operation(
      summary = "Listar todas as OS",
      description = "Lista ordens de serviço com filtros opcionais por status e clienteId.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de Ordens de Serviço",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<OrdemServicoResponse>> listar(
      @Parameter(description = "Filtrar por status da OS (ex: RECEBIDA, EM_DIAGNOSTICO)")
          StatusOS status,
      @Parameter(description = "Filtrar por ID do cliente") UUID clienteId,
      @Parameter(description = "Paginação") Pageable pageable);

  @Operation(
      summary = "Listar fila operacional de OS",
      description =
          "Retorna OS ativas (exclui FINALIZADA, ENTREGUE e CANCELADA), ordenadas por urgência de"
              + " status e depois por data de entrada (mais antigas primeiro).",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Fila operacional retornada com sucesso",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  ResponseEntity<Page<OrdemServicoResponse>> listarFilaOperacional(
      @Parameter(description = "Paginação (ordenação ignorada — fixada por regra de negócio)")
          Pageable pageable);
}
