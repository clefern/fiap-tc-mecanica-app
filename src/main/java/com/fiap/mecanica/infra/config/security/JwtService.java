package com.fiap.mecanica.infra.config.security;

import com.fiap.mecanica.domain.model.TokenPair;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.service.TokenProvider;
import com.fiap.mecanica.infra.entity.RevokedTokenEntity;
import com.fiap.mecanica.infra.jpa.JpaRevokedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService implements TokenProvider {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpiration;

  @Value("${security.jwt.refresh-token.expiration-time}")
  private long refreshExpiration;

  private final JpaRevokedTokenRepository revokedTokenRepository;

  @Override
  public TokenPair generateTokenPair(User user) {
    UserDetails userDetails = new CustomUserDetails(user);
    String accessToken = generateToken(userDetails);
    String refreshToken = generateRefreshToken(userDetails);
    return new TokenPair(accessToken, refreshToken, jwtExpiration / 1000);
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration);
  }

  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), Jwts.SIG.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()))
        && !revokedTokenRepository.existsByToken(token);
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith((javax.crypto.SecretKey) getSignInKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public void revokeToken(String token) {
    try {
      Date expirationDate = extractExpiration(token);
      LocalDateTime expiresAt =
          LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault());

      RevokedTokenEntity revokedToken =
          RevokedTokenEntity.builder().token(token).expiresAt(expiresAt).build();

      revokedTokenRepository.save(revokedToken);
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Token inválido ou já expirado ao tentar revogar: {}", e.getMessage());
    }
  }
}
