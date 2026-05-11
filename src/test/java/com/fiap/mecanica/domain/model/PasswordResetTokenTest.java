package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordResetTokenTest {

  @Test
  @DisplayName("Should correctly identify expired token")
  void shouldIdentifyExpiredToken() {
    PasswordResetToken token =
        new PasswordResetToken(
            UUID.randomUUID(), "token", mock(User.class), LocalDateTime.now().minusMinutes(1));

    assertThat(token.isExpired()).isTrue();
  }

  @Test
  @DisplayName("Should correctly identify valid token")
  void shouldIdentifyValidToken() {
    PasswordResetToken token =
        new PasswordResetToken(
            UUID.randomUUID(), "token", mock(User.class), LocalDateTime.now().plusMinutes(1));

    assertThat(token.isExpired()).isFalse();
  }

  @Test
  @DisplayName("Should cover toString and Getters")
  void shouldCoverToStringAndGetters() {
    UUID id = UUID.randomUUID();
    String tokenStr = "token-123";
    User user = mock(User.class);
    LocalDateTime expiry = LocalDateTime.now();

    PasswordResetToken token = new PasswordResetToken(id, tokenStr, user, expiry);

    assertThat(token.getId()).isEqualTo(id);
    assertThat(token.getToken()).isEqualTo(tokenStr);
    assertThat(token.getUser()).isEqualTo(user);
    assertThat(token.getExpiryDate()).isEqualTo(expiry);
    assertThat(token.toString()).contains(tokenStr);
  }

  @Test
  @DisplayName("Should create token with default expiry")
  void shouldCreateTokenWithDefaultExpiry() {
    User user = mock(User.class);
    PasswordResetToken token = PasswordResetToken.create(user);

    assertThat(token.getUser()).isEqualTo(user);
    assertThat(token.getToken()).isNotEmpty();
    assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now());
    assertThat(token.getExpiryDate()).isBefore(LocalDateTime.now().plusHours(25));
  }
}
