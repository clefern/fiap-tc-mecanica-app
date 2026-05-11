package com.fiap.mecanica.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Value Object para endereço do cliente. Mantém o valor como texto livre (compatível com coluna
 * TEXT) e valida não nulidade e não vazio.
 */
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public final class Endereco {

  private final String value; // texto normalizado (trim)

  private Endereco(String value) {
    this.value = value;
  }

  public static Endereco of(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("Endereço não pode ser nulo");
    }
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Endereço não pode ser vazio");
    }
    return new Endereco(trimmed);
  }

  @Override
  public String toString() {
    return value;
  }
}
