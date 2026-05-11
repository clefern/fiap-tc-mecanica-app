package com.fiap.mecanica.infra.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.valueobject.Email;
import java.util.Collection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class CustomUserDetailsTest {

  @Test
  @DisplayName("Deve retornar detalhes do usuário corretamente")
  void shouldReturnUserDetailsCorrectly() {
    // Arrange
    User user = mock(User.class);
    when(user.getEmail()).thenReturn(Email.of("test@example.com"));
    when(user.getPassword()).thenReturn("password");
    when(user.getRole()).thenReturn(UserRole.MECANICO);
    when(user.isAtivo()).thenReturn(true);

    CustomUserDetails userDetails = new CustomUserDetails(user);

    // Act & Assert
    assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
    assertThat(userDetails.getPassword()).isEqualTo("password");
    assertThat(userDetails.isEnabled()).isTrue();
    assertThat(userDetails.isAccountNonExpired()).isTrue();
    assertThat(userDetails.isAccountNonLocked()).isTrue();
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    assertThat(userDetails.getUser()).isEqualTo(user);

    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertThat(authorities).hasSize(1);
    assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_MECANICO");
  }
}
