package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.AuthenticationService;
import com.fiap.mecanica.domain.model.PasswordResetToken;
import com.fiap.mecanica.domain.model.TokenPair;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.repository.PasswordResetTokenRepository;
import com.fiap.mecanica.domain.repository.UserRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import com.fiap.mecanica.domain.service.TokenProvider;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.config.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetTokenRepository tokenRepository;
  private final NotificationService notificationService;
  private final AuthenticationManager authenticationManager;
  private final TokenProvider tokenProvider;

  public AuthenticationServiceImpl(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      PasswordResetTokenRepository tokenRepository,
      NotificationService notificationService,
      @Lazy AuthenticationManager authenticationManager,
      TokenProvider tokenProvider) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenRepository = tokenRepository;
    this.notificationService = notificationService;
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
  }

  @Override
  @Transactional
  public TokenPair login(String email, String password) {
    log.info("[AUTH_LOGIN] Email={}", email);
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    TokenPair pair = tokenProvider.generateTokenPair(userDetails.getUser());
    log.info("[AUTH_LOGIN_SUCESSO] UserId={}", userDetails.getUser().getId());
    return pair;
  }

  @Override
  @Transactional
  public void resetPassword(String email, String newPassword) {
    log.info("[AUTH_RESET_PASSWORD] Email={}", email);
    if (email == null || newPassword == null) {
      throw new IllegalArgumentException("Email and new password are required");
    }

    User user =
        userRepository
            .findByEmail(Email.of(email))
            .orElseThrow(() -> new RuntimeException("User not found"));

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void forgotPassword(String email) {
    log.info("[AUTH_FORGOT_PASSWORD] Email={}", email);
    User user =
        userRepository
            .findByEmail(Email.of(email))
            .orElseThrow(() -> new RuntimeException("User not found"));

    PasswordResetToken resetToken = PasswordResetToken.create(user);
    tokenRepository.save(resetToken);

    notificationService.sendPasswordResetEmail(user, resetToken.getToken());
  }

  @Override
  @Transactional
  public void resetPasswordWithToken(String token, String newPassword) {
    log.info("[AUTH_RESET_PASSWORD_TOKEN] Token={}", token);
    PasswordResetToken resetToken =
        tokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));

    if (resetToken.isExpired()) {
      tokenRepository.delete(resetToken);
      throw new RuntimeException("Token expired");
    }

    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    tokenRepository.delete(resetToken);
  }
}
