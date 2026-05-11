package com.fiap.mecanica.infra.adapter.email;

import com.fiap.mecanica.application.email.EmailMessage;
import com.fiap.mecanica.application.email.EmailSender;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.service.NotificationService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class SmtpNotificationService implements NotificationService {

  private final EmailSender emailSender;

  @Override
  public void sendPasswordResetEmail(User user, String token) {
    log.info("Enviando email de redefinição de senha para userId={}", user.getId());

    EmailMessage message =
        new EmailMessage(
            List.of(user.getEmail().value()),
            List.of(),
            List.of(),
            "Redefinição de Senha - Mecânica FIAP",
            "email/reset-senha",
            Map.of("user", user, "token", token),
            List.of());

    try {
      emailSender.enviar(message);
      log.info("Email de redefinição enviado com sucesso para userId={}", user.getId());
    } catch (Exception e) {
      log.error("Falha ao enviar email de redefinição para userId={}", user.getId(), e);
    }
  }

  @Override
  public void sendWelcomeEmail(User user, String password) {
    log.info("Enviando email de boas-vindas para userId={}", user.getId());

    EmailMessage message =
        new EmailMessage(
            List.of(user.getEmail().value()),
            List.of(),
            List.of(),
            "Bem-vindo à Mecânica FIAP - Seus dados de acesso",
            "email/bem-vindo",
            Map.of("user", user, "senha", password),
            List.of());

    try {
      emailSender.enviar(message);
      log.info("Email de boas-vindas enviado com sucesso para userId={}", user.getId());
    } catch (Exception e) {
      log.error("Falha ao enviar email de boas-vindas para userId={}", user.getId(), e);
    }
  }
}
