package com.fiap.mecanica.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import com.fiap.mecanica.infra.adapter.JpaUserRepositoryAdapter;
import com.fiap.mecanica.presentation.dto.auth.TokenRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private JpaUserRepositoryAdapter userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    // Create test user
    Cliente user = new Cliente();
    user.setId(UUID.randomUUID());
    user.setNome("Integration Test User");
    user.setEmail(Email.of("integration@test.com"));
    user.setPassword(passwordEncoder.encode("password123"));
    user.setRole(UserRole.CLIENTE);
    user.setDocumento(CPF.of("123.456.789-09")); // Valid CPF
    user.setTipo(TipoPessoa.FISICA);
    user.setTelefone(TelefoneBr.of("11999999999"));
    user.setEndereco(Endereco.of("Rua Teste, 123"));
    user.ativar();

    userRepository.save(user);
  }

  @Test
  @DisplayName("Full Authentication Flow: Login -> Access Protected Resource -> Refresh Token")
  void fullAuthenticationFlow() throws Exception {
    // 1. Login to get token
    TokenRequest loginRequest =
        new TokenRequest("password", "integration@test.com", "password123", null, null, null, null);

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/oauth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").exists())
            .andExpect(jsonPath("$.refresh_token").exists())
            .andReturn();

    TokenResponse tokenResponse =
        objectMapper.readValue(loginResult.getResponse().getContentAsString(), TokenResponse.class);
    String accessToken = tokenResponse.accessToken();
    String refreshToken = tokenResponse.refreshToken();

    // 2. Access protected resource with token
    // Accessing a public endpoint (auth/health) won't test protection.
    // We need a protected endpoint.
    // Let's try /api/clientes which should be protected.
    // Since testUser is a CLIENTE, they might only have access to their own data or listing
    // depending on rules.
    // Assuming /api/clientes/documento/{doc} is protected. Or just /api/clientes (list).

    // Wait, listing customers usually requires ADMIN/ATENDENTE role.
    // But for now, we just want to verify 200 vs 403/401.
    // If Role-based access is strict, 403 is also a success for "Authenticated but unauthorized".
    // But 401 is "Unauthenticated".

    // Let's try to access the user's own profile or a generic endpoint.
    // If I call /api/health (public), it doesn't prove auth.
    // I will use /api/clientes but expect 403 or 200, just not 401.

    // Actually, let's create a simpler test: check if accessing without token gives 401.
    mockMvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized()); // Without token

    // With token, it should NOT be 401. It might be 403 if role is wrong, or 200/404.
    // Since we are CLIENTE, maybe we can't list all clients.
    // Let's try to access a nonexistent URL under /api/ to see if Filter kicks in?
    // No, that gives 404.

    // Let's use the token to access /api/clientes (list)
    // If it returns 403, it means Auth worked (identity known) but Role denied.
    // If it returns 200, Auth worked.
    // If it returns 401, Auth failed.

    // In this system, likely only ADMIN/ATENDENTE can list clients.
    // Let's create an ADMIN user instead for the test?
    // Or just assert status().isForbidden() or isOk().

    mockMvc
        .perform(get("/api/clientes").header("Authorization", "Bearer " + accessToken))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              if (status == 401) {
                throw new AssertionError("Should not be 401 with valid token");
              }
            });

    // 3. Refresh token
    TokenRequest refreshRequest =
        new TokenRequest("refresh_token", null, null, refreshToken, null, null, null);

    mockMvc
        .perform(
            post("/oauth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").exists());
  }
}
