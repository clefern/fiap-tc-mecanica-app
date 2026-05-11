package com.fiap.mecanica.infra.adapter.email;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MockEmailNotificationServiceTest {

  @InjectMocks private MockEmailNotificationService emailService;

  @Test
  @DisplayName("Should send password reset email without error (log check only)")
  void shouldSendPasswordResetEmail() {
    User user = mock(User.class);
    when(user.getId()).thenReturn(UUID.randomUUID());

    assertThatCode(() -> emailService.sendPasswordResetEmail(user, "token-123"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Should send welcome email without error (log check only)")
  void shouldSendWelcomeEmail() {
    User user = mock(User.class);
    when(user.getId()).thenReturn(UUID.randomUUID());

    assertThatCode(() -> emailService.sendWelcomeEmail(user, "password-123"))
        .doesNotThrowAnyException();
  }
}
