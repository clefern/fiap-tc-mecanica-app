package com.fiap.mecanica.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import com.fiap.mecanica.infra.adapter.JpaUserRepositoryAdapter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida que o app aceita transparentemente um JWT HS256 emitido por outro processo (no caso real,
 * a Function Serverless `fiap-tc-mecanica-lambda` que emite após verificar CPF) — desde que o
 * token tenha a mesma secret, algoritmo, e um {@code subject} igual ao email de um usuário
 * cadastrado.
 *
 * <p>Este teste cobre a decisão de ADR-032 (Autenticação de cliente via CPF emitida por Lambda
 * Serverless) sem precisar da Lambda real rodando — emite o JWT diretamente com a mesma biblioteca
 * (jjwt) e secret, simulando exatamente o que a Lambda faz em produção.
 *
 * <p>O smoke test E2E completo com a Lambda deployada em AWS roda em pipeline separado (depende
 * de {@code terraform apply} do Onda 3 do plano de Fase 3).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class JwtFromExternalIssuerIT {

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired private MockMvc mockMvc;
  @Autowired private JpaUserRepositoryAdapter userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Value("${security.jwt.secret-key}")
  private String jwtSecret;

  private Cliente cliente;

  @BeforeEach
  void seedCliente() {
    Cliente novo = new Cliente();
    novo.setNome("Cliente CPF Auth");
    novo.setEmail(Email.of("cliente-cpf-auth@test.com"));
    novo.setPassword(passwordEncoder.encode("not-used-in-this-flow"));
    novo.setRole(UserRole.CLIENTE);
    novo.setDocumento(CPF.of("529.982.247-25")); // CPF válido (módulo 11)
    novo.setTipo(TipoPessoa.FISICA);
    novo.setTelefone(TelefoneBr.of("11999999999"));
    novo.setEndereco(Endereco.of("Rua Teste, 123"));
    novo.ativar();
    cliente = (Cliente) userRepository.save(novo);
  }

  @Test
  @DisplayName(
      "JWT HS256 emitido externamente (mesma secret, subject=email_cadastrado, role=CLIENTE) é"
          + " aceito pelo JwtAuthenticationFilter e libera endpoint com object-level security")
  void externalJwtIsAcceptedAndUnlocksProtectedEndpoint() throws Exception {
    // Simula exatamente o que `fiap-tc-mecanica-lambda/src/handler.ts` faz após validar CPF:
    // emite JWT HS256 com a mesma secret, subject = email do cliente, claims id/role.
    String externalJwt = emitJwtComoLambdaFaria(cliente);

    // Endpoint exige role CLIENTE + object-level security:
    // `@securityService.isOwnerByDocumento(authentication, #documento)` valida que o email do
    // principal (subject do JWT) bate com o email do cliente dono daquele documento.
    mockMvc
        .perform(
            get("/api/clientes/documento/529.982.247-25")
                .header("Authorization", "Bearer " + externalJwt))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("cliente-cpf-auth@test.com"));
  }

  @Test
  @DisplayName("JWT com subject inexistente no banco é rejeitado (401)")
  void externalJwtWithUnknownSubjectIsRejected() throws Exception {
    String tokenForGhost = emitJwt("ghost@notfound.com", Map.of("role", "CLIENTE"));

    mockMvc
        .perform(
            get("/api/clientes/documento/529.982.247-25")
                .header("Authorization", "Bearer " + tokenForGhost))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("JWT assinado com secret diferente é rejeitado (401)")
  void externalJwtWithWrongSecretIsRejected() throws Exception {
    SecretKey wrongKey =
        Keys.hmacShaKeyFor(
            Decoders.BASE64.decode("d3JvbmdTZWNyZXRGb3JUZXN0aW5nUmVqZWN0aW9uMzJCeXRlcw=="));
    String forged =
        Jwts.builder()
            .subject(cliente.getEmail().value())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
            .signWith(wrongKey, Jwts.SIG.HS256)
            .compact();

    mockMvc
        .perform(
            get("/api/clientes/documento/529.982.247-25")
                .header("Authorization", "Bearer " + forged))
        .andExpect(status().isUnauthorized());
  }

  // -- helpers ---------------------------------------------------------------

  private String emitJwtComoLambdaFaria(Cliente c) {
    return emitJwt(
        c.getEmail().value(),
        Map.of(
            "id", c.getId().toString(),
            "role", c.getRole().name()));
  }

  private String emitJwt(String subject, Map<String, Object> extraClaims) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(subject)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
        .signWith(getSignInKey(), Jwts.SIG.HS256)
        .compact();
  }

  private SecretKey getSignInKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }
}
