package com.fiap.mecanica.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CNPJTest {

  @Test
  @DisplayName("Deve criar CNPJ válido")
  void shouldCreateValidCNPJ() {
    String[] validos = {
      "60.701.190/0001-04", // Itau
      "00.000.000/0001-91", // Banco do Brasil
      "33.592.510/0001-54", // Vale
      "06.990.590/0001-23" // Google
    };

    for (String valid : validos) {
      CNPJ cnpj = new CNPJ(valid);
      assertNotNull(cnpj);
      assertEquals(valid.replaceAll("\\D", ""), cnpj.valor());
      assertEquals(valid, cnpj.formatado());
    }
  }

  @Test
  @DisplayName("Deve criar CNPJ válido sem formatação")
  void shouldCreateValidCNPJUnhormatted() {
    String valid = "60701190000104";
    CNPJ cnpj = new CNPJ(valid);
    assertNotNull(cnpj);
    assertEquals(valid, cnpj.valor());
    assertEquals("60.701.190/0001-04", cnpj.formatado());
  }

  @Test
  @DisplayName("Não deve criar CNPJ nulo")
  void shouldThrowWhenNull() {
    assertThrows(IllegalArgumentException.class, () -> new CNPJ(null));
  }

  @Test
  @DisplayName("Não deve criar CNPJ com tamanho inválido")
  void shouldThrowWhenInvalidLength() {
    assertThrows(IllegalArgumentException.class, () -> new CNPJ("123"));
  }

  @Test
  @DisplayName("Não deve criar CNPJ com dígitos iguais")
  void shouldThrowWhenAllDigitsEqual() {
    assertThrows(IllegalArgumentException.class, () -> new CNPJ("11111111111111"));
  }

  @Test
  @DisplayName("Não deve criar CNPJ com dígitos verificadores inválidos")
  void shouldThrowWhenInvalidCheckDigits() {
    // CNPJ com tamanho correto mas dígitos verificadores errados
    assertThrows(IllegalArgumentException.class, () -> new CNPJ("39053344000100"));

    // Primeiro dígito correto, segundo incorreto (Base: 60.701.190/0001-04)
    assertThrows(IllegalArgumentException.class, () -> new CNPJ("60701190000105"));

    // Primeiro dígito incorreto (Base: 60.701.190/0001-04)
    assertThrows(IllegalArgumentException.class, () -> new CNPJ("60701190000114"));
  }
}
