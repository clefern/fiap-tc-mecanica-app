package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.AuthenticationService;
import com.fiap.mecanica.domain.model.TokenPair;
import com.fiap.mecanica.domain.model.command.ResetPasswordCommand;
import com.fiap.mecanica.infra.config.security.JwtService;
import com.fiap.mecanica.infra.config.security.SecurityProperties;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.AuthApi;
import com.fiap.mecanica.presentation.dto.auth.ForgotPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.ResetPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenResponse;
import com.fiap.mecanica.presentation.mapper.AuthMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class AuthController implements AuthApi {

  private static final String ERROR_KEY = "error";

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final AuthenticationService authenticationService;
  private final AuthMapper authMapper;
  private final Bucket bucket;

  @Value("${security.jwt.expiration-time}")
  private long accessTokenValidity;

  public AuthController(
      JwtService jwtService,
      UserDetailsService userDetailsService,
      AuthenticationService authenticationService,
      AuthMapper authMapper,
      SecurityProperties securityProperties) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.authenticationService = authenticationService;
    this.authMapper = authMapper;

    // Rate limiting configured via properties
    Bandwidth limit =
        Bandwidth.builder()
            .capacity(securityProperties.getCapacity())
            .refillGreedy(
                securityProperties.getRefillTokens(),
                Duration.ofMinutes(securityProperties.getRefillDurationMinutes()))
            .build();
    this.bucket = Bucket.builder().addLimit(limit).build();
  }

  @Override
  @PostMapping("/token")
  public ResponseEntity<Object> getToken(@RequestBody @Valid TokenRequest request) {
    if (!bucket.tryConsume(1)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(Map.of(ERROR_KEY, "Too many requests"));
    }

    if ("password".equals(request.grantType())) {
      return handlePasswordGrant(request);
    } else if ("refresh_token".equals(request.grantType())) {
      return handleRefreshTokenGrant(request);
    } else if ("client_credentials".equals(request.grantType())) {
      return ResponseEntity.badRequest()
          .body(Map.of(ERROR_KEY, "client_credentials not implemented yet"));
    } else if ("authorization_code".equals(request.grantType())) {
      return ResponseEntity.badRequest()
          .body(Map.of(ERROR_KEY, "authorization_code not implemented yet"));
    }

    return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "unsupported_grant_type"));
  }

  private ResponseEntity<Object> handlePasswordGrant(TokenRequest request) {
    try {
      TokenPair tokenPair = authenticationService.login(request.username(), request.password());

      return ResponseEntity.ok(
          new TokenResponse(
              tokenPair.accessToken(),
              tokenPair.refreshToken(),
              "Bearer",
              tokenPair.expiresInSeconds()));
    } catch (AuthenticationException | IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of(ERROR_KEY, "invalid_grant", "error_description", e.getMessage()));
    }
  }

  private ResponseEntity<Object> handleRefreshTokenGrant(TokenRequest request) {
    String refreshToken = request.refreshToken();
    if (refreshToken == null) {
      return ResponseEntity.badRequest()
          .body(Map.of(ERROR_KEY, "invalid_request", "error_description", "Missing refresh token"));
    }

    try {
      String username = jwtService.extractUsername(refreshToken);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (jwtService.isTokenValid(refreshToken, userDetails)) {
        String accessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return ResponseEntity.ok(
            new TokenResponse(accessToken, newRefreshToken, "Bearer", accessTokenValidity / 1000));
      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of(ERROR_KEY, "invalid_grant", "error_description", "Invalid refresh token"));
      }
    } catch (AuthenticationException | JwtException | IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of(ERROR_KEY, "invalid_grant", "error_description", "Invalid refresh token"));
    }
  }

  @Override
  @PostMapping("/validate")
  public ResponseEntity<Object> validateToken(@RequestBody Map<String, String> request) {
    String token = request.get("token");
    if (token == null) {
      return ResponseEntity.badRequest().body(Map.of("active", false));
    }

    try {
      String username = jwtService.extractUsername(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      boolean isValid = jwtService.isTokenValid(token, userDetails);
      return ResponseEntity.ok(Map.of("active", isValid, "username", username));
    } catch (AuthenticationException | JwtException | IllegalArgumentException e) {
      return ResponseEntity.ok(Map.of("active", false));
    }
  }

  @Override
  @PostMapping("/revoke")
  public ResponseEntity<Object> revokeToken(
      @RequestHeader(value = "Authorization", required = false) String authHeader,
      @RequestParam(required = false) String token) {
    String tokenToRevoke = token;
    if (tokenToRevoke == null && authHeader != null && authHeader.startsWith("Bearer ")) {
      tokenToRevoke = authHeader.substring(7);
    }

    if (tokenToRevoke != null) {
      jwtService.revokeToken(tokenToRevoke);
      return ResponseEntity.ok(Map.of("message", "Token revoked successfully"));
    }

    return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "No token provided"));
  }

  @Override
  @GetMapping("/authorize")
  public ResponseEntity<Object> authorize(@RequestParam Map<String, String> params) {
    return ResponseEntity.ok(Map.of("message", "placeholder"));
  }

  @Override
  @PostMapping("/forgot-password")
  public ResponseEntity<Object> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
    String email = authMapper.toDomain(request);
    authenticationService.forgotPassword(email);
    return ResponseEntity.ok(Map.of("message", "If email exists, reset link sent."));
  }

  @Override
  @PostMapping("/reset-password")
  public ResponseEntity<Object> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
    ResetPasswordCommand command = authMapper.toDomain(request);
    authenticationService.resetPasswordWithToken(command.getToken(), command.getNewPassword());
    return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
  }
}
