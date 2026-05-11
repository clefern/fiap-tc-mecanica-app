package com.fiap.mecanica.infra.config.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.valueobject.Documento;
import com.fiap.mecanica.domain.valueobject.Email;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

  @Mock private ClienteRepository clienteRepository;

  private SecurityService securityService;

  @BeforeEach
  void setUp() {
    securityService = new SecurityService(clienteRepository);
  }

  @Test
  @DisplayName("Deve retornar false se autenticação for nula")
  void shouldReturnFalseIfAuthenticationIsNull() {
    assertFalse(securityService.isOwnerByDocumento(null, "12345678909"));
  }

  @Test
  @DisplayName("Deve retornar false se não estiver autenticado")
  void shouldReturnFalseIfNotAuthenticated() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(false);
    assertFalse(securityService.isOwnerByDocumento(auth, "12345678909"));
  }

  @Test
  @DisplayName("Deve retornar false se cliente não encontrado")
  void shouldReturnFalseIfClienteNotFound() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getPrincipal()).thenReturn("user@example.com");

    // CPF válido
    String cpfValido = "12345678909";

    when(clienteRepository.findByDocumento(any(Documento.class))).thenReturn(Optional.empty());

    assertFalse(securityService.isOwnerByDocumento(auth, cpfValido));
  }

  @Test
  @DisplayName("Deve retornar false se email não corresponder (Principal String)")
  void shouldReturnFalseIfEmailDoesNotMatchStringPrincipal() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getPrincipal()).thenReturn("other@example.com");

    Cliente cliente = mock(Cliente.class);
    when(cliente.getEmail()).thenReturn(Email.of("user@example.com"));

    String cpfValido = "12345678909";
    when(clienteRepository.findByDocumento(any(Documento.class))).thenReturn(Optional.of(cliente));

    assertFalse(securityService.isOwnerByDocumento(auth, cpfValido));
  }

  @Test
  @DisplayName("Deve retornar true se email corresponder (Principal String)")
  void shouldReturnTrueIfEmailMatchesStringPrincipal() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getPrincipal()).thenReturn("user@example.com");

    Cliente cliente = mock(Cliente.class);
    when(cliente.getEmail()).thenReturn(Email.of("user@example.com"));

    String cpfValido = "12345678909";
    when(clienteRepository.findByDocumento(any(Documento.class))).thenReturn(Optional.of(cliente));

    assertTrue(securityService.isOwnerByDocumento(auth, cpfValido));
  }

  @Test
  @DisplayName(
      "Deve retornar true se email corresponder (Principal UserDetails) e documento for CNPJ")
  void shouldReturnTrueIfEmailMatchesUserDetailsPrincipalAndCNPJ() {
    Authentication auth = mock(Authentication.class);
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("user@example.com");

    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getPrincipal()).thenReturn(userDetails);

    Cliente cliente = mock(Cliente.class);
    when(cliente.getEmail()).thenReturn(Email.of("user@example.com"));

    String cnpjValido = "00000000000191";
    when(clienteRepository.findByDocumento(any(Documento.class))).thenReturn(Optional.of(cliente));

    assertTrue(securityService.isOwnerByDocumento(auth, cnpjValido));
  }
}
