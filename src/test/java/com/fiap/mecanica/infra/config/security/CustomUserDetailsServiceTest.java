package com.fiap.mecanica.infra.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.repository.UserRepository;
import com.fiap.mecanica.domain.valueobject.Email;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private CustomUserDetailsService userDetailsService;

  @Test
  @DisplayName("Should load user by username successfully")
  void shouldLoadUserByUsername() {
    String email = "test@example.com";
    User user = new Cliente();
    user.setId(UUID.randomUUID());
    user.setEmail(Email.of(email));
    user.setPassword("password");
    user.setRole(UserRole.CLIENTE);
    user.ativar();

    when(userRepository.findByEmail(Email.of(email))).thenReturn(Optional.of(user));

    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(email);
    assertThat(userDetails.getPassword()).isEqualTo("password");
    assertThat(userDetails.getAuthorities()).hasSize(1);
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
        .isEqualTo("ROLE_CLIENTE");
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void shouldThrowExceptionWhenUserNotFound() {
    String email = "notfound@example.com";
    when(userRepository.findByEmail(Email.of(email))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("User not found");
  }
}
