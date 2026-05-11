package com.fiap.mecanica.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.AuthenticationService;
import com.fiap.mecanica.domain.model.TokenPair;
import com.fiap.mecanica.domain.model.command.ResetPasswordCommand;
import com.fiap.mecanica.infra.config.security.JwtService;
import com.fiap.mecanica.infra.config.security.SecurityProperties;
import com.fiap.mecanica.presentation.dto.auth.ForgotPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.ResetPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenResponse;
import com.fiap.mecanica.presentation.mapper.AuthMapper;
import io.github.bucket4j.Bucket;
import io.jsonwebtoken.JwtException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtService jwtService;

  @Mock private UserDetailsService userDetailsService;

  @Mock private AuthenticationService authenticationService;

  @Mock private AuthMapper authMapper;

  @Mock private SecurityProperties securityProperties;

  private AuthController authController;

  @Mock private Bucket mockBucket;

  @BeforeEach
  void setUp() {
    // Stub properties to avoid constructor errors
    when(securityProperties.getCapacity()).thenReturn(10);
    when(securityProperties.getRefillTokens()).thenReturn(10);
    when(securityProperties.getRefillDurationMinutes()).thenReturn(1);

    authController =
        new AuthController(
            jwtService, userDetailsService, authenticationService, authMapper, securityProperties);

    ReflectionTestUtils.setField(authController, "bucket", mockBucket);
    ReflectionTestUtils.setField(authController, "accessTokenValidity", 3600000L);
  }

  @Test
  @DisplayName("Should return token for valid password grant")
  void shouldReturnTokenForValidPasswordGrant() {
    TokenRequest request = new TokenRequest("password", "user", "pass", null, null, null, null);
    when(mockBucket.tryConsume(1)).thenReturn(true);
    when(authenticationService.login("user", "pass"))
        .thenReturn(new TokenPair("access-token", "refresh-token", 3600));

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    TokenResponse body = (TokenResponse) response.getBody();
    assertThat(body.accessToken()).isEqualTo("access-token");
    assertThat(body.refreshToken()).isEqualTo("refresh-token");
  }

  @Test
  @DisplayName("Should return 401 for invalid credentials")
  void shouldReturn401ForInvalidCredentials() {
    TokenRequest request = new TokenRequest("password", "user", "wrong", null, null, null, null);

    when(mockBucket.tryConsume(1)).thenReturn(true);
    when(authenticationService.login("user", "wrong"))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("Should return 401 when IllegalArgumentException in password grant")
  void shouldReturn401WhenIllegalArgumentExceptionInPasswordGrant() {
    TokenRequest request = new TokenRequest("password", "user", "pass", null, null, null, null);

    when(mockBucket.tryConsume(1)).thenReturn(true);
    when(authenticationService.login("user", "pass"))
        .thenThrow(new IllegalArgumentException("Invalid args"));

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(((Map) response.getBody()).get("error")).isEqualTo("invalid_grant");
  }

  @Test
  @DisplayName("Should return 429 when rate limit exceeded")
  void shouldReturn429WhenRateLimitExceeded() {
    TokenRequest request = new TokenRequest("password", "user", "pass", null, null, null, null);
    when(mockBucket.tryConsume(1)).thenReturn(false);

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }

  @Test
  @DisplayName("Should return 400 for client_credentials grant type")
  void shouldReturn400ForClientCredentials() {
    TokenRequest request =
        new TokenRequest("client_credentials", null, null, null, null, null, null);
    when(mockBucket.tryConsume(1)).thenReturn(true);

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(((Map) response.getBody()).get("error"))
        .isEqualTo("client_credentials not implemented yet");
  }

  @Test
  @DisplayName("Should return 400 for authorization_code grant type")
  void shouldReturn400ForAuthorizationCode() {
    TokenRequest request =
        new TokenRequest("authorization_code", null, null, null, null, null, null);
    when(mockBucket.tryConsume(1)).thenReturn(true);

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(((Map) response.getBody()).get("error"))
        .isEqualTo("authorization_code not implemented yet");
  }

  @Test
  @DisplayName("Should return 400 for unsupported grant type")
  void shouldReturn400ForUnsupportedGrantType() {
    TokenRequest request = new TokenRequest("unknown", null, null, null, null, null, null);
    when(mockBucket.tryConsume(1)).thenReturn(true);

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(((Map) response.getBody()).get("error")).isEqualTo("unsupported_grant_type");
  }

  @Test
  @DisplayName("Should return token for valid refresh_token grant")
  void shouldReturnTokenForValidRefreshTokenGrant() {
    TokenRequest request =
        new TokenRequest("refresh_token", null, null, "valid-refresh-token", null, null, null);
    UserDetails userDetails = mock(UserDetails.class);

    when(mockBucket.tryConsume(1)).thenReturn(true);
    when(jwtService.extractUsername("valid-refresh-token")).thenReturn("user");
    when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
    when(jwtService.isTokenValid("valid-refresh-token", userDetails)).thenReturn(true);
    when(jwtService.generateToken(userDetails)).thenReturn("new-access-token");
    when(jwtService.generateRefreshToken(userDetails)).thenReturn("new-refresh-token");

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    TokenResponse body = (TokenResponse) response.getBody();
    assertThat(body.accessToken()).isEqualTo("new-access-token");
    assertThat(body.refreshToken()).isEqualTo("new-refresh-token");
  }

  @Test
  @DisplayName("Should return 400 for missing refresh token")
  void shouldReturn400ForMissingRefreshToken() {
    TokenRequest request = new TokenRequest("refresh_token", null, null, null, null, null, null);
    when(mockBucket.tryConsume(1)).thenReturn(true);

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(((Map) response.getBody()).get("error")).isEqualTo("invalid_request");
  }

  @Test
  @DisplayName("Should return 401 for invalid refresh token")
  void shouldReturn401ForInvalidRefreshToken() {
    TokenRequest request =
        new TokenRequest("refresh_token", null, null, "invalid-token", null, null, null);
    UserDetails userDetails = mock(UserDetails.class);

    when(mockBucket.tryConsume(1)).thenReturn(true);
    when(jwtService.extractUsername("invalid-token")).thenReturn("user");
    when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
    when(jwtService.isTokenValid("invalid-token", userDetails)).thenReturn(false);

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(((Map) response.getBody()).get("error")).isEqualTo("invalid_grant");
  }

  @Test
  @DisplayName("Should return 401 when exception occurs during refresh token grant")
  void shouldReturn401WhenExceptionDuringRefreshToken() {
    TokenRequest request =
        new TokenRequest("refresh_token", null, null, "error-token", null, null, null);

    when(mockBucket.tryConsume(1)).thenReturn(true);
    when(jwtService.extractUsername("error-token")).thenThrow(new JwtException("Token error"));

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(((Map) response.getBody()).get("error")).isEqualTo("invalid_grant");
  }

  @Test
  @DisplayName("Should return 401 when IllegalArgumentException in refresh token grant")
  void shouldReturn401WhenIllegalArgumentExceptionInRefreshTokenGrant() {
    TokenRequest request =
        new TokenRequest("refresh_token", null, null, "error-token", null, null, null);

    when(mockBucket.tryConsume(1)).thenReturn(true);
    when(jwtService.extractUsername("error-token"))
        .thenThrow(new IllegalArgumentException("Invalid args"));

    ResponseEntity<?> response = authController.getToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(((Map) response.getBody()).get("error")).isEqualTo("invalid_grant");
  }

  @Test
  @DisplayName("Should return placeholder for authorize endpoint")
  void shouldReturnPlaceholderForAuthorizeEndpoint() {
    ResponseEntity<?> response = authController.authorize(Map.of("client_id", "123"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsKey("message");
    assertThat(body.get("message").toString()).contains("placeholder");
  }

  @Test
  @DisplayName("Should validate token successfully")
  void shouldValidateTokenSuccessfully() {
    String token = "valid-token";
    String username = "user";
    UserDetails userDetails = mock(UserDetails.class);
    Map<String, String> request = Map.of("token", token);

    when(jwtService.extractUsername(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

    ResponseEntity<?> response = authController.validateToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("active")).isEqualTo(true);
    assertThat(body.get("username")).isEqualTo(username);
  }

  @Test
  @DisplayName("Should return active false for invalid token")
  void shouldReturnActiveFalseForInvalidToken() {
    String token = "invalid-token";
    String username = "user";
    UserDetails userDetails = mock(UserDetails.class);
    Map<String, String> request = Map.of("token", token);

    when(jwtService.extractUsername(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

    ResponseEntity<?> response = authController.validateToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("active")).isEqualTo(false);
  }

  @Test
  @DisplayName("Should return active false when exception occurs during validation")
  void shouldReturnActiveFalseWhenExceptionOccurs() {
    String token = "error-token";
    Map<String, String> request = Map.of("token", token);

    when(jwtService.extractUsername(token)).thenThrow(new JwtException("Token error"));

    ResponseEntity<?> response = authController.validateToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("active")).isEqualTo(false);
  }

  @Test
  @DisplayName("Should return active false when AuthenticationException occurs during validation")
  void shouldReturnActiveFalseWhenAuthenticationExceptionOccurs() {
    String token = "error-token";
    Map<String, String> request = Map.of("token", token);

    when(jwtService.extractUsername(token)).thenThrow(new BadCredentialsException("Bad creds"));

    ResponseEntity<?> response = authController.validateToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("active")).isEqualTo(false);
  }

  @Test
  @DisplayName("Should return active false when IllegalArgumentException occurs during validation")
  void shouldReturnActiveFalseWhenIllegalArgumentExceptionOccurs() {
    String token = "error-token";
    Map<String, String> request = Map.of("token", token);

    when(jwtService.extractUsername(token)).thenThrow(new IllegalArgumentException("Invalid args"));

    ResponseEntity<?> response = authController.validateToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("active")).isEqualTo(false);
  }

  @Test
  @DisplayName("Should return 400 when token is missing")
  void shouldReturn400WhenTokenIsMissing() {
    Map<String, String> request = Map.of(); // Empty map

    ResponseEntity<?> response = authController.validateToken(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("active")).isEqualTo(false);
  }

  @Test
  @DisplayName("Should revoke token successfully")
  void shouldRevokeTokenSuccessfully() {
    String token = "valid-token";

    ResponseEntity<?> response = authController.revokeToken(null, token);

    verify(jwtService).revokeToken(token);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("message")).isEqualTo("Token revoked successfully");
  }

  @Test
  @DisplayName("Should revoke token from header")
  void shouldRevokeTokenFromHeader() {
    String token = "valid-token";
    String authHeader = "Bearer " + token;

    ResponseEntity<?> response = authController.revokeToken(authHeader, null);

    verify(jwtService).revokeToken(token);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @DisplayName("Should return 400 when no token provided for revocation")
  void shouldReturn400WhenNoTokenForRevocation() {
    ResponseEntity<?> response = authController.revokeToken(null, null);

    verify(jwtService, never()).revokeToken(any());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("error")).isEqualTo("No token provided");
  }

  @Test
  @DisplayName("Should return 400 when header is malformed for revocation")
  void shouldReturn400WhenHeaderIsMalformedForRevocation() {
    String authHeader = "Basic some-token";
    ResponseEntity<?> response = authController.revokeToken(authHeader, null);

    verify(jwtService, never()).revokeToken(any());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body.get("error")).isEqualTo("No token provided");
  }

  @Test
  @DisplayName("Should call forgotPassword service")
  void shouldCallForgotPasswordService() {
    ForgotPasswordRequest request = new ForgotPasswordRequest("test@test.com");
    when(authMapper.toDomain(request)).thenReturn("test@test.com");

    ResponseEntity<?> response = authController.forgotPassword(request);

    verify(authenticationService).forgotPassword("test@test.com");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @DisplayName("Should call resetPasswordWithToken service")
  void shouldCallResetPasswordWithTokenService() {
    ResetPasswordRequest request =
        new ResetPasswordRequest("valid-token", "test@example.com", "new-pass");
    ResetPasswordCommand command =
        ResetPasswordCommand.builder().token("valid-token").newPassword("new-pass").build();

    when(authMapper.toDomain(request)).thenReturn(command);

    ResponseEntity<?> response = authController.resetPassword(request);

    verify(authenticationService).resetPasswordWithToken("valid-token", "new-pass");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
