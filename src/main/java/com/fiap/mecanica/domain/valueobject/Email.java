package com.fiap.mecanica.domain.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Value Object para Email com validação simples RFC-like. Normaliza para minúsculas, rejeitando
 * entradas inválidas.
 */
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public final class Email {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

  private final String value; // sempre normalizado (lowercase)

  private Email(String normalized) {
    this.value = normalized;
  }

  public static Email of(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("Email não pode ser nulo");
    }
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Email não pode ser vazio");
    }
    String lower = trimmed.toLowerCase(Locale.ROOT);
    if (!EMAIL_PATTERN.matcher(lower).matches()) {
      throw new IllegalArgumentException("Email inválido");
    }
    return new Email(lower);
  }

  @Override
  public String toString() {
    return value;
  }
}
