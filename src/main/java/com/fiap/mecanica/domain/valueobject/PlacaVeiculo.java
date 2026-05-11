package com.fiap.mecanica.domain.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Value Object para placa de veículo (Brasil). Aceita padrões: - Legado: AAA-1234 (normaliza para
 * AAA1234) - Mercosul: ABC1D23
 */
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public final class PlacaVeiculo {

  private static final Pattern LEGADO = Pattern.compile("^[A-Za-z]{3}-?\\d{4}$");
  private static final Pattern MERCOSUL = Pattern.compile("^[A-Za-z]{3}\\d{1}[A-Za-z]{1}\\d{2}$");

  private final String value; // normalizado para uppercase, sem hífen

  private PlacaVeiculo(String normalized) {
    this.value = normalized;
  }

  public static PlacaVeiculo of(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("Placa não pode ser nula");
    }
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Placa não pode ser vazia");
    }
    String upper = trimmed.toUpperCase(Locale.ROOT);

    if (MERCOSUL.matcher(upper).matches()) {
      return new PlacaVeiculo(upper);
    }
    if (LEGADO.matcher(upper).matches()) {
      String normalized = upper.replace("-", "");
      return new PlacaVeiculo(normalized);
    }
    throw new IllegalArgumentException("Placa inválida");
  }

  @Override
  public String toString() {
    return value;
  }
}
