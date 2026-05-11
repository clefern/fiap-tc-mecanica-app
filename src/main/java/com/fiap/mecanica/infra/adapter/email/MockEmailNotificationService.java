package com.fiap.mecanica.infra.adapter.email;

import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class MockEmailNotificationService implements NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(MockEmailNotificationService.class);

  @Override
  public void sendPasswordResetEmail(User user, String token) {
    logger.info("MOCK: email de redefinição de senha enviado para userId={}", user.getId());
  }

  @Override
  public void sendWelcomeEmail(User user, String password) {
    logger.info("MOCK: email de boas-vindas enviado para userId={}", user.getId());
  }
}
