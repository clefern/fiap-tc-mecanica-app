package com.fiap.mecanica.presentation.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TelefoneValidatorTest {
  private final TelefoneValidator validator = new TelefoneValidator();

  @Test
  @DisplayName("Deve aceitar telefones válidos (fixo e celular) e rejeitar inválidos")
  void shouldValidateTelefone() {
    assertThat(validator.isValid("(11) 91234-5678", null)).isTrue(); // celular
    assertThat(validator.isValid("11 2345-6789", null)).isTrue(); // fixo
    assertThat(validator.isValid("00 1234-5678", null)).isFalse(); // DDD inválido
    assertThat(validator.isValid("11 123-456", null)).isFalse(); // formato inválido
    assertThat(validator.isValid("", null)).isFalse();
    assertThat(validator.isValid(null, null)).isTrue();
  }
}
