package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.AprovacaoOrcamentoExternaRequest;
import com.fiap.mecanica.presentation.dto.OrcamentoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
    name = "Integrações",
    description = "Endpoints para integração machine-to-machine com sistemas externos")
public interface IntegracaoOrcamentoApi {

  @Operation(
      summary = "Aprovação/reprovação externa de orçamento (machine-to-machine)",
      description =
          "Recebe notificação externa de decisão do cliente sobre o orçamento. "
              + "Autenticação via header **X-Api-Key** (sem JWT necessário).",
      security = @SecurityRequirement(name = "ApiKeyAuth"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Decisão processada com sucesso",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "API key inválida ou ausente"),
        @ApiResponse(
            responseCode = "404",
            description = "OS não encontrada ou sem orçamento pendente"),
        @ApiResponse(
            responseCode = "422",
            description = "Transição de status inválida para o orçamento")
      })
  ResponseEntity<OrcamentoResponse> processarAprovacaoExterna(
      @RequestBody(description = "Código da OS e decisão (APROVADO ou REPROVADO)", required = true)
          AprovacaoOrcamentoExternaRequest request);

  @Operation(
      summary = "Aprovação/reprovação de orçamento via link de email",
      description =
          "Processa a decisão do cliente ao clicar no link de aprovação ou recusa enviado por"
              + " email. Autenticação via **token HMAC-SHA256** embutido na URL (sem JWT ou API"
              + " key). O token é gerado no envio do email e expira em 24 horas.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Decisão processada com sucesso",
            content = @Content(schema = @Schema(implementation = OrcamentoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido, adulterado ou expirado"),
        @ApiResponse(responseCode = "404", description = "Orçamento não encontrado"),
        @ApiResponse(
            responseCode = "422",
            description = "Transição de status inválida para o orçamento")
      })
  ResponseEntity<OrcamentoResponse> processarAprovacaoPorToken(
      @Parameter(
              description =
                  "Token HMAC-SHA256 gerado no email. Encoda o ID do orçamento, a decisão e a"
                      + " expiração.",
              required = true)
          String token);
}
