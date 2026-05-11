package com.fiap.mecanica.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthTokenDTOTest {

  @Test
  @DisplayName("Deve expor campos corretamente")
  void shouldExposeFieldsCorrectly() {
    AuthTokenDTO dto = new AuthTokenDTO("access", "refresh", "Bearer", 3600L);

    assertThat(dto.accessToken()).isEqualTo("access");
    assertThat(dto.refreshToken()).isEqualTo("refresh");
    assertThat(dto.tokenType()).isEqualTo("Bearer");
    assertThat(dto.expiresIn()).isEqualTo(3600L);
  }
}
