package com.fiap.mecanica.infra.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class UserContextTest {

  private final UserContext userContext = new UserContext();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Deve retornar null quando não autenticado")
  void shouldReturnNullWhenNotAuthenticated() {
    SecurityContextHolder.clearContext();

    UUID result = userContext.getAuthenticatedUserId();

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Deve retornar null quando principal não é CustomUserDetails")
  void shouldReturnNullWhenPrincipalIsNotCustomUserDetails() {
    Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UUID result = userContext.getAuthenticatedUserId();

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Deve retornar ID do usuário autenticado quando principal é CustomUserDetails")
  void shouldReturnUserIdWhenPrincipalIsCustomUserDetails() {
    UUID userId = UUID.randomUUID();
    Cliente user = new Cliente();
    user.setId(userId);
    user.setNome("Teste");
    user.setDocumento(CPF.of("529.982.247-25"));
    user.setEmail(Email.of("teste@teste.com"));
    user.setTelefone(TelefoneBr.of("11999999999"));
    user.setEndereco(Endereco.of("Rua A"));
    user.setRole(UserRole.CLIENTE);
    user.ativar();

    CustomUserDetails userDetails = new CustomUserDetails(user);
    Authentication auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    UUID result = userContext.getAuthenticatedUserId();

    assertThat(result).isEqualTo(userId);
  }
}
