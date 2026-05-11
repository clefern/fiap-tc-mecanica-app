package com.fiap.mecanica.domain.model;

public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {}
