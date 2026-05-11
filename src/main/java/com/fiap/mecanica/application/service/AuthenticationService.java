package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.TokenPair;

public interface AuthenticationService {
  TokenPair login(String email, String password);

  void resetPassword(String email, String newPassword);

  void forgotPassword(String email);

  void resetPasswordWithToken(String token, String newPassword);
}
