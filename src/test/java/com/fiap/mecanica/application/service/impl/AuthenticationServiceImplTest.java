package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.model.PasswordResetToken;
import com.fiap.mecanica.domain.model.TokenPair;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.repository.PasswordResetTokenRepository;
import com.fiap.mecanica.domain.repository.UserRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import com.fiap.mecanica.domain.service.TokenProvider;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.config.security.CustomUserDetails;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private PasswordResetTokenRepository tokenRepository;
  @Mock private NotificationService notificationService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private TokenProvider tokenProvider;

  @InjectMocks private AuthenticationServiceImpl service;

  private User user;

  @BeforeEach
  void setUp() {
    user = new Atendente();
    user.setId(UUID.randomUUID());
    user.setEmail(Email.of("test@example.com"));
    user.setPassword("encodedPassword");
  }

  @Test
  @DisplayName("Should login successfully")
  void shouldLoginSuccessfully() {
    String email = "test@example.com";
    String password = "rawPassword";
    Authentication authentication = mock(Authentication.class);
    CustomUserDetails userDetails = mock(CustomUserDetails.class);
    TokenPair expectedPair = new TokenPair("access", "refresh", 3600L);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUser()).thenReturn(user);
    when(tokenProvider.generateTokenPair(user)).thenReturn(expectedPair);

    TokenPair result = service.login(email, password);

    assertThat(result).isEqualTo(expectedPair);
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  @DisplayName("Should throw exception when resetting password with null email or password")
  void shouldThrowExceptionWhenResettingPasswordWithNullEmailOrPassword() {
    assertThatThrownBy(() -> service.resetPassword(null, "newPassword"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email and new password are required");

    assertThatThrownBy(() -> service.resetPassword("email@test.com", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email and new password are required");
  }

  @Test
  @DisplayName("Should reset password successfully")
  void shouldResetPasswordSuccessfully() {
    String email = "test@example.com";
    String newPassword = "newPassword";
    String encodedNewPassword = "encodedNewPassword";

    when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

    service.resetPassword(email, newPassword);

    verify(userRepository).save(user);
    assertThat(user.getPassword()).isEqualTo(encodedNewPassword);
  }

  @Test
  @DisplayName("Should throw exception when resetting password for unknown user")
  void shouldThrowExceptionWhenResettingPasswordForUnknownUser() {
    String email = "unknown@example.com";
    String newPassword = "newPassword";

    when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.resetPassword(email, newPassword))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("User not found");
  }

  @Test
  @DisplayName("Should handle forgot password successfully")
  void shouldHandleForgotPasswordSuccessfully() {
    String email = "test@example.com";

    when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));

    service.forgotPassword(email);

    verify(tokenRepository).save(any(PasswordResetToken.class));
    verify(notificationService).sendPasswordResetEmail(eq(user), any(String.class));
  }

  @Test
  @DisplayName("Should throw exception when forgot password for unknown user")
  void shouldThrowExceptionWhenForgotPasswordForUnknownUser() {
    String email = "unknown@example.com";
    when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.forgotPassword(email))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("User not found");
  }

  @Test
  @DisplayName("Should reset password with token successfully")
  void shouldResetPasswordWithTokenSuccessfully() {
    String token = "validToken";
    String newPassword = "newPassword";
    String encodedNewPassword = "encodedNewPassword";
    PasswordResetToken resetToken = PasswordResetToken.create(user);
    // Ensure token is not expired (it's created now)

    when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
    when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

    service.resetPasswordWithToken(token, newPassword);

    verify(userRepository).save(user);
    verify(tokenRepository).delete(resetToken); // Assuming delete is called after usage
    assertThat(user.getPassword()).isEqualTo(encodedNewPassword);
  }

  @Test
  @DisplayName("Should throw exception for invalid token")
  void shouldThrowExceptionForInvalidToken() {
    String token = "invalidToken";
    String newPassword = "newPassword";

    when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.resetPasswordWithToken(token, newPassword))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Invalid token");
  }

  @Test
  @DisplayName("Should throw exception for expired token")
  void shouldThrowExceptionForExpiredToken() {
    String token = "expiredToken";
    String newPassword = "newPassword";
    PasswordResetToken resetToken = mock(PasswordResetToken.class);

    when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
    when(resetToken.isExpired()).thenReturn(true);

    assertThatThrownBy(() -> service.resetPasswordWithToken(token, newPassword))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Token expired");

    verify(tokenRepository).delete(resetToken);
  }
}
