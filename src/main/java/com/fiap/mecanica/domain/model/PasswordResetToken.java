package com.fiap.mecanica.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class PasswordResetToken {
  private final UUID id;
  private final String token;
  private final User user;
  private final LocalDateTime expiryDate;

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiryDate);
  }

  public static PasswordResetToken create(User user) {
    return new PasswordResetToken(
        null, UUID.randomUUID().toString(), user, LocalDateTime.now().plusHours(24));
  }
}
