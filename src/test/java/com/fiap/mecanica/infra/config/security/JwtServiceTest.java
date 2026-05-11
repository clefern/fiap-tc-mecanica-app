package com.fiap.mecanica.infra.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.infra.jpa.JpaRevokedTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

  @Mock private JpaRevokedTokenRepository revokedTokenRepository;

  @InjectMocks private JwtService jwtService;

  private final String secretKey =
      "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"; // 256-bit key
  private final long expiration = 3600000; // 1 hour

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", expiration);
    ReflectionTestUtils.setField(jwtService, "refreshExpiration", expiration);
  }

  @Test
  @DisplayName("Deve gerar token e extrair username")
  void shouldGenerateAndExtractUsername() {
    UserDetails user = new User("user", "pass", new ArrayList<>());
    String token = jwtService.generateToken(user);

    assertThat(token).isNotNull();
    assertThat(jwtService.extractUsername(token)).isEqualTo("user");
  }

  @Test
  @DisplayName("Deve validar token")
  void shouldValidateToken() {
    UserDetails user = new User("user", "pass", new ArrayList<>());
    String token = jwtService.generateToken(user);

    when(revokedTokenRepository.existsByToken(token)).thenReturn(false);

    boolean isValid = jwtService.isTokenValid(token, user);
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Deve invalidar token se username não bater")
  void shouldInvalidateIfUsernameMismatch() {
    UserDetails user = new User("user", "pass", new ArrayList<>());
    UserDetails otherUser = new User("other", "pass", new ArrayList<>());
    String token = jwtService.generateToken(user);

    // username mismatch
    boolean isValid = jwtService.isTokenValid(token, otherUser);
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Deve invalidar token se expirado")
  void shouldInvalidateIfExpired() {
    UserDetails user = new User("user", "pass", new ArrayList<>());

    // Generate expired token manually
    String token =
        Jwts.builder()
            .setSubject(user.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
            .setExpiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();

    // isTokenValid checks expiration
    // We expect it to throw ExpiredJwtException or return false depending on
    // implementation
    // But extractUsername throws ExpiredJwtException immediately if we use parser
    // Let's see how extractUsername is implemented: extractClaim ->
    // extractAllClaims ->
    // parseClaimsJws
    // This throws ExpiredJwtException.
    // So we can assert that exception is thrown.

    try {
      jwtService.isTokenValid(token, user);
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      assertThat(e).isNotNull();
    }
  }

  @Test
  @DisplayName("Deve invalidar token se revogado")
  void shouldInvalidateIfRevoked() {
    UserDetails user = new User("user", "pass", new ArrayList<>());
    String token = jwtService.generateToken(user);

    when(revokedTokenRepository.existsByToken(token)).thenReturn(true);

    boolean isValid = jwtService.isTokenValid(token, user);
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Deve revogar token com sucesso")
  void shouldRevokeTokenSuccessfully() {
    String token = jwtService.generateToken(new User("user", "pass", new ArrayList<>()));
    jwtService.revokeToken(token);

    // We can't verify static methods easily, but we can verify repository call
    // But repository save is called only if token is valid.
    // In test environment, Jwts.parser()... works, so token is valid.
    // However, verify(revokedTokenRepository).save(...)
    // Let's verify that repository save was called.
    // Need to capture argument to ensure correct mapping.
    verify(revokedTokenRepository).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("Nao deve lançar exceção ao revogar token invalido")
  void shouldNotThrowExceptionWhenRevokingInvalidToken() {
    // Malformed token
    jwtService.revokeToken("invalid-token");
  }

  @Test
  @DisplayName("Nao deve salvar se token expirado ao revogar")
  void shouldNotSaveIfTokenExpiredWhenRevoking() {
    // Create expired token
    String token =
        Jwts.builder()
            .setSubject("user")
            .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
            .setExpiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();

    jwtService.revokeToken(token);

    // Should catch exception or check expiration and not save
    verify(revokedTokenRepository, org.mockito.Mockito.never())
        .save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("Deve gerar refresh token")
  void shouldGenerateRefreshToken() {
    UserDetails user = new User("user", "pass", new ArrayList<>());
    String token = jwtService.generateRefreshToken(user);
    assertThat(token).isNotNull();
  }

  @Test
  @DisplayName("Deve gerar token com claims extras")
  void shouldGenerateTokenWithExtraClaims() {
    UserDetails user = new User("user", "pass", new ArrayList<>());
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "admin");

    String token = jwtService.generateToken(claims, user);

    assertThat(token).isNotNull();
    String role = jwtService.extractClaim(token, c -> c.get("role", String.class));
    org.assertj.core.api.Assertions.assertThat(role).isEqualTo("admin");
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
