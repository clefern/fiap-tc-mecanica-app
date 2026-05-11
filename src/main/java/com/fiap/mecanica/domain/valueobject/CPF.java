package com.fiap.mecanica.domain.valueobject;

import lombok.EqualsAndHashCode;

/**
 * Value Object para CPF com validação pelo algoritmo oficial. Normaliza para apenas dígitos (11
 * caracteres) e rejeita entradas inválidas.
 */
@EqualsAndHashCode
public final class CPF implements Documento {

  private final String value; // sempre 11 dígitos

  private CPF(String normalized) {
    this.value = normalized;
  }

  public static CPF of(String raw) {
    String normalized = normalize(raw);
    if (!isValidCpf(normalized)) {
      throw new IllegalArgumentException("CPF inválido");
    }
    return new CPF(normalized);
  }

  @Override
  public String valor() {
    return value;
  }

  @Override
  public String formatado() {
    return value.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
  }

  @Override
  public String toString() {
    return value;
  }

  private static String normalize(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("CPF não pode ser nulo");
    }
    String digits = raw.replaceAll("\\D", "");
    if (digits.isBlank()) {
      throw new IllegalArgumentException("CPF não pode ser vazio");
    }
    return digits;
  }

  private static boolean isValidCpf(String digits) {
    if (digits.length() != 11) {
      return false;
    }
    // rejeita todos dígitos iguais
    if (digits.chars().distinct().count() == 1) {
      return false;
    }

    int d1 = calcDigit(digits.substring(0, 9), 10);
    int d2 = calcDigit(digits.substring(0, 9) + d1, 11);
    return digits.equals(digits.substring(0, 9) + d1 + d2);
  }

  private static int calcDigit(String base, int weightStart) {
    int sum = 0;
    for (int i = 0; i < base.length(); i++) {
      int num = base.charAt(i) - '0';
      sum += num * (weightStart - i);
    }
    int mod = sum % 11;
    return mod < 2 ? 0 : 11 - mod;
  }
}
