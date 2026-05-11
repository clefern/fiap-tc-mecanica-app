package com.fiap.mecanica.infra.config.security;

import com.fiap.mecanica.presentation.dto.DecisaoOrcamento;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Generates and validates short-lived HMAC-SHA256 action tokens for email links.
 *
 * <p>Token format: {@code base64url(payload) + "." + base64url(HMAC-SHA256(encodedPayload))}
 * Payload: {@code "orcamentoId:DECISAO:expiryEpochSeconds"}
 */
@Service
@Slf4j
public class ActionTokenService {

  private static final String HMAC_ALGORITHM = "HmacSHA256";

  @Value("${mecanica.mail.action-token-secret}")
  private String secret;

  @Value("${mecanica.mail.action-token-expiry-minutes}")
  private int expiryMinutes;

  /**
   * Generates a signed action token encoding the orcamento ID, decision, and expiry.
   *
   * @param orcamentoId the orçamento UUID
   * @param decisao APROVADO or REPROVADO
   * @return signed token string safe for use in URLs
   */
  public String generate(UUID orcamentoId, DecisaoOrcamento decisao) {
    long expiryEpoch = Instant.now().plusSeconds(expiryMinutes * 60L).getEpochSecond();
    String payload = orcamentoId + ":" + decisao.name() + ":" + expiryEpoch;
    String encodedPayload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    String hmac = computeHmac(encodedPayload);
    return encodedPayload + "." + hmac;
  }

  /**
   * Validates a token and returns its payload if the signature is valid and not expired.
   *
   * @param token the token string from the URL parameter
   * @return the payload, or empty if invalid or expired
   */
  public Optional<ActionTokenPayload> validate(String token) {
    if (token == null || !token.contains(".")) {
      return Optional.empty();
    }

    int dotIndex = token.lastIndexOf('.');
    String encodedPayload = token.substring(0, dotIndex);
    String providedHmac = token.substring(dotIndex + 1);

    String expectedHmac = computeHmac(encodedPayload);
    if (!expectedHmac.equals(providedHmac)) {
      log.warn("[ACTION_TOKEN] Assinatura inválida");
      return Optional.empty();
    }

    String payload;
    try {
      payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      log.warn("[ACTION_TOKEN] Payload com encoding inválido");
      return Optional.empty();
    }

    String[] parts = payload.split(":");
    if (parts.length != 3) {
      return Optional.empty();
    }

    try {
      UUID orcamentoId = UUID.fromString(parts[0]);
      DecisaoOrcamento decisao = DecisaoOrcamento.valueOf(parts[1]);
      long expiryEpoch = Long.parseLong(parts[2]);

      if (Instant.now().getEpochSecond() > expiryEpoch) {
        log.warn("[ACTION_TOKEN] Token expirado para orcamentoId={}", orcamentoId);
        return Optional.empty();
      }

      return Optional.of(new ActionTokenPayload(orcamentoId, decisao));
    } catch (IllegalArgumentException e) {
      log.warn("[ACTION_TOKEN] Token com formato inválido: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private String computeHmac(String data) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      SecretKeySpec keySpec =
          new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
      mac.init(keySpec);
      byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
    } catch (Exception e) {
      throw new IllegalStateException("Erro ao computar HMAC para action token", e);
    }
  }
}
