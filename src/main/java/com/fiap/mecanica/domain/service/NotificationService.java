package com.fiap.mecanica.domain.service;

import com.fiap.mecanica.domain.model.User;

public interface NotificationService {
  void sendPasswordResetEmail(User user, String token);

  void sendWelcomeEmail(User user, String password);
}
