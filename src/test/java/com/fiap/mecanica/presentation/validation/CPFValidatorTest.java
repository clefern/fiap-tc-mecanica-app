package com.fiap.mecanica.presentation.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CPFValidatorTest {
  private final CPFValidator validator = new CPFValidator();

  @Test
  @DisplayName("Deve aceitar CPFs válidos e rejeitar inválidos")
  void shouldValidateCPF() {
    validator.initialize(null); // Coverage for empty method
    assertThat(validator.isValid("529.982.247-25", null)).isTrue();
    assertThat(validator.isValid("390.533.447-05", null)).isTrue();
    assertThat(validator.isValid("000.000.000-00", null)).isFalse();
    assertThat(validator.isValid("11111111111", null)).isFalse();
    assertThat(validator.isValid("", null)).isFalse();
    assertThat(validator.isValid(null, null)).isTrue();
  }
}
