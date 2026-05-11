package com.fiap.mecanica.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultPasswordPolicyTest {

  @Test
  @DisplayName("Should generate random password")
  void shouldGenerateRandomPassword() {
    DefaultPasswordPolicy policy = new DefaultPasswordPolicy();
    String password = policy.generateRandomPassword();
    assertThat(password).isNotNull().hasSize(8);
  }
}
