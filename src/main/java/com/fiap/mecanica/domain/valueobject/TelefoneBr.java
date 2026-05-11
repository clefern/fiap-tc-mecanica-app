package com.fiap.mecanica.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Value Object de telefone brasileiro: DDD (2 dígitos) + número (8 ou 9 dígitos). Normaliza para
 * apenas dígitos. Não inclui código do país.
 */
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public final class TelefoneBr {

  private final String value; // DDD + número (10 ou 11 dígitos)

  private TelefoneBr(String normalized) {
    this.value = normalized;
  }

  public static TelefoneBr of(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("Telefone não pode ser nulo");
    }
    String digits = raw.replaceAll("\\D", "");
    if (digits.isBlank()) {
      throw new IllegalArgumentException("Telefone não pode ser vazio");
    }

    if (digits.length() != 10 && digits.length() != 11) {
      throw new IllegalArgumentException("Telefone deve ter 10 ou 11 dígitos (DDD + número)");
    }
    String ddd = digits.substring(0, 2);
    String numero = digits.substring(2);

    if (!isValidDDD(ddd)) {
      throw new IllegalArgumentException("DDD inválido");
    }
    if (!isValidNumero(numero)) {
      throw new IllegalArgumentException("Número inválido");
    }

    return new TelefoneBr(ddd + numero);
  }

  private static boolean isValidDDD(String ddd) {
    // DDD brasileiro: 11..99 (não iniciar com 0 e não ser 10)
    // Como ddd vem de substring(0, 2), é garantido ter 2 dígitos, logo <= 99 é
    // sempre true
    int d = Integer.parseInt(ddd);
    return d >= 11;
  }

  private static boolean isValidNumero(String numero) {
    // 8 ou 9 dígitos; primeiro dígito não pode ser 0 ou 1
    // Length check is guaranteed by 'of' method (total 10 or 11 - 2 DDD = 8 or 9)
    // Regex removed non-digits, so it's guaranteed to be <= '9'
    char first = numero.charAt(0);
    return first >= '2';
  }

  @Override
  public String toString() {
    return value;
  }
}
