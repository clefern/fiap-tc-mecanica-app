package com.fiap.mecanica.application.dto;

public record AuthTokenDTO(
    String accessToken, String refreshToken, String tokenType, long expiresIn) {}
