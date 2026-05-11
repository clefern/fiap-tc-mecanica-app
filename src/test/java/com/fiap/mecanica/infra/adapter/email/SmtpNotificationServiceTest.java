package com.fiap.mecanica.infra.adapter.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.email.EmailMessage;
import com.fiap.mecanica.application.email.EmailSender;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.valueobject.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SmtpNotificationServiceTest {

  @Mock private EmailSender emailSender;

  @InjectMocks private SmtpNotificationService service;

  @Test
  @DisplayName("Deve enviar email de boas-vindas")
  void shouldSendWelcomeEmail() {
    User user = mock(User.class);
    when(user.getEmail()).thenReturn(Email.of("teste@teste.com"));

    service.sendWelcomeEmail(user, "senha123");

    verify(emailSender).enviar(any(EmailMessage.class));
  }

  @Test
  @DisplayName("Deve enviar email de redefinição de senha")
  void shouldSendPasswordResetEmail() {
    User user = mock(User.class);
    when(user.getEmail()).thenReturn(Email.of("teste@teste.com"));

    service.sendPasswordResetEmail(user, "token123");

    verify(emailSender).enviar(any(EmailMessage.class));
  }

  @Test
  @DisplayName("Deve tratar erro ao enviar email de boas-vindas")
  void shouldHandleErrorOnWelcomeEmail() {
    User user = mock(User.class);
    when(user.getEmail()).thenReturn(Email.of("teste@teste.com"));

    org.mockito.Mockito.doThrow(new RuntimeException("erro email"))
        .when(emailSender)
        .enviar(any(EmailMessage.class));

    service.sendWelcomeEmail(user, "senha123");
  }

  @Test
  @DisplayName("Deve tratar erro ao enviar email de redefinição")
  void shouldHandleErrorOnPasswordResetEmail() {
    User user = mock(User.class);
    when(user.getEmail()).thenReturn(Email.of("teste@teste.com"));

    org.mockito.Mockito.doThrow(new RuntimeException("erro email"))
        .when(emailSender)
        .enviar(any(EmailMessage.class));

    service.sendPasswordResetEmail(user, "token123");
  }
}
