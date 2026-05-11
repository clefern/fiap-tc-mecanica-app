package com.fiap.mecanica.presentation.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com tokens de acesso")
public record TokenResponse(
    @Schema(
            description = "Token de acesso JWT",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @JsonProperty("access_token")
        String accessToken,
    @Schema(
            description = "Token de atualização",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @JsonProperty("refresh_token")
        String refreshToken,
    @Schema(description = "Tipo do token", example = "Bearer") @JsonProperty("token_type")
        String tokenType,
    @Schema(description = "Tempo de expiração em segundos", example = "3600")
        @JsonProperty("expires_in")
        long expiresIn) {}
