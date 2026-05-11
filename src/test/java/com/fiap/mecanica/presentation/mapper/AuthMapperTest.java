package com.fiap.mecanica.presentation.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.model.command.ResetPasswordCommand;
import com.fiap.mecanica.presentation.dto.auth.ForgotPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.ResetPasswordRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthMapperTest {

  private final AuthMapper mapper = new AuthMapper();

  @Test
  @DisplayName("Deve converter ForgotPasswordRequest para email (String) corretamente")
  void shouldConvertForgotPasswordRequestToDomain() {
    // Arrange
    String email = "test@example.com";
    ForgotPasswordRequest request = ForgotPasswordRequest.builder().email(email).build();

    // Act
    String result = mapper.toDomain(request);

    // Assert
    assertEquals(email, result);
  }

  @Test
  @DisplayName("Deve retornar null ao converter ForgotPasswordRequest nulo")
  void shouldReturnNullWhenConvertingNullForgotPasswordRequest() {
    assertNull(mapper.toDomain((ForgotPasswordRequest) null));
  }

  @Test
  @DisplayName("Deve converter ResetPasswordRequest para Command corretamente")
  void shouldConvertResetPasswordRequestToDomain() {
    // Arrange
    String token = "valid-token";
    String newPassword = "new-secure-password";
    ResetPasswordRequest request =
        ResetPasswordRequest.builder().token(token).newPassword(newPassword).build();

    // Act
    ResetPasswordCommand command = mapper.toDomain(request);

    // Assert
    assertNotNull(command);
    assertEquals(token, command.getToken());
    assertEquals(newPassword, command.getNewPassword());
  }

  @Test
  @DisplayName("Deve retornar null ao converter ResetPasswordRequest nulo")
  void shouldReturnNullWhenConvertingNullResetPasswordRequest() {
    assertNull(mapper.toDomain((ResetPasswordRequest) null));
  }
}
