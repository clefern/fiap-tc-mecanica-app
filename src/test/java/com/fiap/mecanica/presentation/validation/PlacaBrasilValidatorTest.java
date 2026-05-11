package com.fiap.mecanica.presentation.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlacaBrasilValidatorTest {
  private final PlacaBrasilValidator validator = new PlacaBrasilValidator();

  @Test
  @DisplayName("Should accept valid Mercosul and legacy plates and reject invalid ones")
  void shouldValidatePlaca() {
    // Mercosul
    assertThat(validator.isValid("ABC1D23", null)).isTrue();
    // Legado with hyphen
    assertThat(validator.isValid("AAA-1234", null)).isTrue();
    // Legado without hyphen
    assertThat(validator.isValid("BBB1234", null)).isTrue();

    // Invalid formats
    assertThat(validator.isValid("ABCD123", null)).isFalse();
    assertThat(validator.isValid("AA-12345", null)).isFalse();
    assertThat(validator.isValid("", null)).isFalse();

    // Null is valid to allow composition with @NotNull
    assertThat(validator.isValid(null, null)).isTrue();
  }
}
