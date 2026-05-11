package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenPairTest {

  @Test
  @DisplayName("Should create TokenPair correctly")
  void shouldCreateTokenPairCorrectly() {
    String accessToken = "access-token";
    String refreshToken = "refresh-token";
    long expiresIn = 3600L;

    TokenPair tokenPair = new TokenPair(accessToken, refreshToken, expiresIn);

    assertThat(tokenPair.accessToken()).isEqualTo(accessToken);
    assertThat(tokenPair.refreshToken()).isEqualTo(refreshToken);
    assertThat(tokenPair.expiresInSeconds()).isEqualTo(expiresIn);
  }

  @Test
  @DisplayName("Should test equality and hash code")
  void shouldTestEqualityAndHashCode() {
    TokenPair token1 = new TokenPair("a", "b", 10L);
    TokenPair token2 = new TokenPair("a", "b", 10L);
    TokenPair token3 = new TokenPair("x", "y", 20L);

    assertThat(token1).isEqualTo(token2);
    assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    assertThat(token1).isNotEqualTo(token3);
  }

  @Test
  @DisplayName("Should test toString")
  void shouldTestToString() {
    TokenPair token = new TokenPair("acc", "ref", 100L);
    assertThat(token.toString()).contains("acc", "ref", "100");
  }
}
