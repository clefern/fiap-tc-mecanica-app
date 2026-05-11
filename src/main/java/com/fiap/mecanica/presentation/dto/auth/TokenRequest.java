package com.fiap.mecanica.presentation.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para obtenção de token OAuth2")
public record TokenRequest(
    @Schema(
            description = "Tipo de concessão (grant type)",
            example = "password",
            allowableValues = {"password", "refresh_token"})
        @NotBlank
        @JsonProperty("grant_type")
        String grantType,
    @Schema(
            description = "Nome de usuário (obrigatório para grant_type=password)",
            example = "admin")
        String username,
    @Schema(description = "Senha (obrigatório para grant_type=password)", example = "123456")
        String password,
    @Schema(
            description = "Token de atualização (obrigatório para grant_type=refresh_token)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @JsonProperty("refresh_token")
        String refreshToken,
    @Schema(description = "ID do cliente (opcional)", example = "my-client-app")
        @JsonProperty("client_id")
        String clientId,
    @Schema(description = "Segredo do cliente (opcional)", example = "secret")
        @JsonProperty("client_secret")
        String clientSecret,
    @Schema(
            description = "Código de autorização (para grant_type=authorization_code)",
            example = "code_123")
        String code) {}
