package com.fiap.mecanica.presentation.api;

import com.fiap.mecanica.presentation.dto.auth.ForgotPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.ResetPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@Tag(
    name = "Autenticação",
    description =
        "Endpoints de autenticação. \n\n"
            + "**Clientes (CPF)**: usar `POST /auth` no Traefik gateway — roteia para a Function\n"
            + "Serverless (Lambda CPF→JWT). O JWT emitido pela Lambda é aceito transparentemente\n"
            + "pelos endpoints `/api/*` (mesma secret HS256). Ver ADR-032.\n\n"
            + "**Equipe interna (ADMIN, ATENDENTE, MECANICO)**: usar `POST /oauth/token` com\n"
            + "`grant_type=password` (email + senha). Este endpoint está marcado como `@Deprecated`\n"
            + "para clientes — mantido apenas para staff interna.")
public interface AuthApi {

  @Operation(
      summary = "Obter token de acesso (uso restrito a staff interna)",
      description =
          "Obtém tokens de acesso e refresh usando `grant_type=password` (login) ou"
              + " `grant_type=refresh_token` (renovação).\n\n"
              + "**Deprecated para clientes (CPF)** — clientes devem usar `POST /auth` no Traefik"
              + " gateway, que delega para a Function Serverless. Este endpoint permanece"
              + " ativo para login de staff interna (ADMIN, ATENDENTE, MECANICO).\n\n"
              + "Ver ADR-032: `docs/ADRs/ADR-032-autenticacao-cpf-via-lambda.md`.",
      deprecated = true,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Token gerado com sucesso",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida ou tipo de concessão não suportado"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou token expirado"),
        @ApiResponse(responseCode = "429", description = "Muitas requisições — rate limit excedido")
      })
  ResponseEntity<?> getToken(
      @Parameter(description = "Dados da requisição de token", required = true)
          TokenRequest request);

  @Operation(
      summary = "Validar token",
      description = "Verifica se um token JWT é válido e está ativo. Retorna claims do token.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Resultado da validação",
            content =
                @Content(
                    schema =
                        @Schema(example = "{\"active\": true, \"username\": \"user@email.com\"}")))
      })
  ResponseEntity<?> validateToken(
      @Parameter(
              description = "JSON contendo o token a ser validado: {\"token\": \"...\"}",
              required = true)
          Map<String, String> request);

  @Operation(
      summary = "Revogar token",
      description = "Invalida um token de acesso, impedindo seu uso em requisições futuras.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Token revogado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nenhum token fornecido")
      })
  ResponseEntity<?> revokeToken(
      @Parameter(description = "Header de autorização (Bearer token)") String authHeader,
      @Parameter(description = "Token passado como query param (opcional)") String token);

  @Operation(
      summary = "Autorizar (Placeholder)",
      description = "Endpoint de autorização (placeholder para fluxo Authorization Code).",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Sucesso",
            content = @Content(schema = @Schema(example = "{\"message\": \"placeholder\"}")))
      })
  ResponseEntity<?> authorize(
      @Parameter(description = "Parâmetros de autorização") Map<String, String> params);

  @Operation(
      summary = "Solicitar redefinição de senha",
      description =
          "Envia um email com link de redefinição de senha para o endereço informado."
              + " O link expira em 1 hora.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Email de redefinição enviado (se o endereço existir)"),
        @ApiResponse(responseCode = "400", description = "Email inválido ou ausente"),
        @ApiResponse(
            responseCode = "429",
            description = "Muitas tentativas — tente novamente em alguns minutos")
      })
  ResponseEntity<?> forgotPassword(ForgotPasswordRequest request);

  @Operation(
      summary = "Redefinir senha com token",
      description =
          "Redefine a senha do usuário usando o token recebido por email."
              + " O token é de uso único e expira em 1 hora.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
        @ApiResponse(
            responseCode = "400",
            description = "Token inválido, expirado ou senha não atende aos requisitos"),
        @ApiResponse(responseCode = "404", description = "Token não encontrado")
      })
  ResponseEntity<?> resetPassword(ResetPasswordRequest request);
}
