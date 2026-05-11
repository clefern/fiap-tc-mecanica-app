package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.presentation.dto.OrdemServicoResponse;
import com.fiap.mecanica.presentation.dto.TrocarMecanicoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
    name = "Ações da OS",
    description =
        "Transições de estado da Ordem de Serviço. Cada ação corresponde a uma etapa do fluxo:"
            + " RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → APROVADA → EM_EXECUCAO"
            + " → FINALIZADA → ENTREGUE | CANCELADA")
@SecurityRequirement(name = "bearerAuth")
public interface OrdemServicoAcoesApi {

  @Operation(
      summary = "Trocar mecânico responsável",
      description =
          "Altera o mecânico responsável pela execução. Disponível em qualquer status ativo."
              + " Requer perfil ADMIN.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Mecânico alterado com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Requer perfil ADMIN"),
        @ApiResponse(
            responseCode = "404",
            description = "Ordem de Serviço ou Mecânico não encontrado")
      })
  ResponseEntity<OrdemServico> trocarMecanico(
      @PathVariable UUID id, @RequestBody TrocarMecanicoRequest request);

  @Operation(
      summary = "Iniciar diagnóstico",
      description =
          "Transita a OS de RECEBIDA para EM_DIAGNOSTICO. Atribui o mecânico autenticado como"
              + " responsável. Requer perfil MECANICO.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Diagnóstico iniciado com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "403",
            description = "Requer perfil MECANICO ou OS pertence a outro mecânico"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
        @ApiResponse(responseCode = "422", description = "Transição de status inválida")
      })
  ResponseEntity<OrdemServico> iniciarDiagnostico(@PathVariable UUID id);

  @Operation(
      summary = "Emitir orçamento",
      description =
          "Finaliza o diagnóstico e transita para AGUARDANDO_APROVACAO, gerando um orçamento"
              + " com base nos itens adicionados. Requer pelo menos um item na OS.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Orçamento emitido com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "403",
            description = "Requer perfil MECANICO ou OS pertence a outro mecânico"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
        @ApiResponse(
            responseCode = "422",
            description = "Transição inválida ou OS sem itens cadastrados")
      })
  ResponseEntity<OrdemServico> finalizarDiagnostico(@PathVariable UUID id);

  @Operation(
      summary = "Iniciar execução",
      description =
          "Transita de APROVADA para EM_EXECUCAO. Requer mecânico atribuído e orçamento aprovado.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Execução iniciada com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "403",
            description = "Requer perfil MECANICO ou OS pertence a outro mecânico"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
        @ApiResponse(
            responseCode = "422",
            description = "Transição inválida ou mecânico não atribuído")
      })
  ResponseEntity<OrdemServico> iniciarExecucao(@PathVariable UUID id);

  @Operation(
      summary = "Finalizar execução",
      description = "Transita de EM_EXECUCAO para FINALIZADA. Requer perfil MECANICO.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Serviço finalizado com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "403",
            description = "Requer perfil MECANICO ou OS pertence a outro mecânico"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
        @ApiResponse(responseCode = "422", description = "Transição de status inválida")
      })
  ResponseEntity<OrdemServico> finalizar(@PathVariable UUID id);

  @Operation(
      summary = "Registrar entrega do veículo",
      description = "Transita de FINALIZADA para ENTREGUE. Requer perfil ATENDENTE ou ADMIN.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Veículo entregue com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Requer perfil ATENDENTE ou ADMIN"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
        @ApiResponse(responseCode = "422", description = "Transição de status inválida")
      })
  ResponseEntity<OrdemServico> entregar(@PathVariable UUID id);

  @Operation(
      summary = "Cancelar OS",
      description =
          "Transita para CANCELADA a partir de qualquer status ativo. Requer perfil ATENDENTE,"
              + " ADMIN ou mecânico responsável.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OS cancelada com sucesso",
            content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(
            responseCode = "403",
            description = "Requer perfil ATENDENTE, ADMIN ou ser o mecânico responsável"),
        @ApiResponse(responseCode = "404", description = "Ordem de Serviço não encontrada"),
        @ApiResponse(responseCode = "422", description = "Transição de status inválida")
      })
  ResponseEntity<OrdemServico> cancelar(@PathVariable UUID id);
}
