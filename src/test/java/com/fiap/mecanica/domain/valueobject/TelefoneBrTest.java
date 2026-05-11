package com.fiap.mecanica.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TelefoneBrTest {

  @Test
  @DisplayName("Deve aceitar celular com DDD e normalizar para dígitos")
  void shouldAcceptMobile() {
    TelefoneBr tel1 = TelefoneBr.of("(11) 91234-5678");
    assertThat(tel1.value()).isEqualTo("11912345678");

    TelefoneBr tel2 = TelefoneBr.of("11 912345678");
    assertThat(tel2.value()).isEqualTo("11912345678");
  }

  @Test
  @DisplayName("Deve aceitar fixo com DDD e normalizar para dígitos")
  void shouldAcceptLandline() {
    TelefoneBr tel = TelefoneBr.of("11 2345-6789");
    assertThat(tel.value()).isEqualTo("1123456789");
  }

  @Test
  @DisplayName("Não deve aceitar DDD inválido ou número inválido")
  void shouldRejectInvalid() {
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("01 91234-5678"));
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("10 91234-5678"));
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("11 01234-5678"));
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("11 1234567"));
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of(null));
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("   "));
  }

  @Test
  @DisplayName("Deve rejeitar DDD inválido (limites)")
  void shouldRejectInvalidDDD() {
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("00 12345678"));
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("10 12345678"));
    // DDD 99 is valid, but we can test logic with invalid inputs
  }

  @Test
  @DisplayName("Deve rejeitar número inválido (primeiro dígito)")
  void shouldRejectInvalidNumberStart() {
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("11 01234567"));
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("11 11234567"));
  }

  @Test
  @DisplayName("equals/hashCode/toString devem considerar o valor normalizado")
  void equalsHashCodeAndToString() {
    TelefoneBr t1 = TelefoneBr.of("(11) 91234-5678");
    TelefoneBr t2 = TelefoneBr.of("11 912345678");

    assertThat(t1).isEqualTo(t2);
    assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    assertThat(t1.toString()).isEqualTo("11912345678");
  }

  @Test
  @DisplayName("Deve rejeitar string vazia após normalização")
  void shouldRejectEmptyAfterNormalization() {
    assertThrows(IllegalArgumentException.class, () -> TelefoneBr.of("abc"));
  }
}
