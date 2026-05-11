package com.fiap.mecanica.presentation.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailNormalizedValidatorTest {
  private final EmailNormalizedValidator validator = new EmailNormalizedValidator();

  @Test
  @DisplayName("Should accept valid emails and reject invalid ones; null passes for composition")
  void shouldValidateEmail() {
    assertThat(validator.isValid("USER@Example.COM", null)).isTrue();
    assertThat(validator.isValid("john.doe+alias@example.co", null)).isTrue();

    assertThat(validator.isValid("invalid@", null)).isFalse();
    assertThat(validator.isValid("@example.com", null)).isFalse();
    assertThat(validator.isValid("", null)).isFalse();

    assertThat(validator.isValid(null, null)).isTrue();
  }
}
