package com.fiap.mecanica.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnderecoTest {

  @Test
  @DisplayName("Deve normalizar com trim e aceitar texto válido")
  void shouldNormalizeAndAccept() {
    Endereco e = Endereco.of("  Rua Exemplo, 123 - Centro, Cidade/UF   ");
    assertThat(e.value()).isEqualTo("Rua Exemplo, 123 - Centro, Cidade/UF");
  }

  @Test
  @DisplayName("Não deve aceitar nulo ou vazio")
  void shouldRejectNullOrEmpty() {
    assertThrows(IllegalArgumentException.class, () -> Endereco.of(null));
    assertThrows(IllegalArgumentException.class, () -> Endereco.of("   "));
  }

  @Test
  @DisplayName("equals/hashCode/toString devem considerar o valor normalizado")
  void equalsHashCodeAndToString() {
    Endereco e1 = Endereco.of("Rua A, 1 - Bairro, Cidade/UF");
    Endereco e2 = Endereco.of("  Rua A, 1 - Bairro, Cidade/UF   ");

    assertThat(e1).isEqualTo(e2);
    assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    assertThat(e1.toString()).isEqualTo("Rua A, 1 - Bairro, Cidade/UF");
  }
}
