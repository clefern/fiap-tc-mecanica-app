package com.fiap.mecanica.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordGeneratorTest {

  @Test
  @DisplayName("Should generate password with default length")
  void shouldGeneratePasswordWithDefaultLength() {
    String password = PasswordGenerator.generate();
    assertThat(password).hasSize(8);
  }

  @Test
  @DisplayName("Should generate password with custom length")
  void shouldGeneratePasswordWithCustomLength() {
    String password = PasswordGenerator.generate(12);
    assertThat(password).hasSize(12);
  }

  @Test
  @DisplayName("Should generate random passwords")
  void shouldGenerateRandomPasswords() {
    String pass1 = PasswordGenerator.generate();
    String pass2 = PasswordGenerator.generate();
    assertThat(pass1).isNotEqualTo(pass2);
  }
}
