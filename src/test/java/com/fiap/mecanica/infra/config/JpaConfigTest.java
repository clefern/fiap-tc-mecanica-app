package com.fiap.mecanica.infra.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JpaConfigTest {

  @Test
  void shouldInstantiate() {
    JpaConfig config = new JpaConfig();
    assertThat(config).isNotNull();
  }
}
