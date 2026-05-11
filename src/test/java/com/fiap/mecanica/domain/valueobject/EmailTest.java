package com.fiap.mecanica.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  @DisplayName("Deve aceitar email válido e normalizar para minúsculas")
  void shouldAcceptValidEmail() {
    Email email = Email.of("User.Name+tag@Example.COM");
    assertThat(email.value()).isEqualTo("user.name+tag@example.com");
  }

  @Test
  @DisplayName("Não deve aceitar email inválido")
  void shouldRejectInvalidEmail() {
    assertThrows(IllegalArgumentException.class, () -> Email.of("invalid"));
    assertThrows(IllegalArgumentException.class, () -> Email.of("user@"));
    assertThrows(IllegalArgumentException.class, () -> Email.of("@example.com"));
    assertThrows(IllegalArgumentException.class, () -> Email.of("user@example"));
  }

  @Test
  @DisplayName("Não deve aceitar nulo ou vazio")
  void shouldRejectNullOrEmpty() {
    assertThrows(IllegalArgumentException.class, () -> Email.of(null));
    assertThrows(IllegalArgumentException.class, () -> Email.of(""));
    assertThrows(IllegalArgumentException.class, () -> Email.of("   "));
  }

  @Test
  @DisplayName("equals/hashCode/toString devem considerar email normalizado")
  void equalsHashCodeAndToString() {
    Email e1 = Email.of("User@Example.com");
    Email e2 = Email.of("user@example.COM");

    // igualdade por valor normalizado
    assertThat(e1).isEqualTo(e2);
    assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    // toString deve devolver o valor normalizado
    assertThat(e1.toString()).isEqualTo("user@example.com");
  }
}
