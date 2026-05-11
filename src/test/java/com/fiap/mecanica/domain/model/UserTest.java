package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.valueobject.Email;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

  // Concrete implementation for testing abstract class
  private static class TestUser extends User {
    public TestUser(
        UUID id, String nome, Email email, String password, UserRole role, boolean ativo) {
      super(id, nome, email, password, role, ativo);
    }
  }

  @Test
  @DisplayName("Should activate and deactivate user")
  void shouldActivateAndDeactivateUser() {
    TestUser user =
        new TestUser(
            UUID.randomUUID(),
            "Test User",
            Email.of("test@example.com"),
            "password",
            UserRole.MECANICO,
            true);

    assertThat(user.isAtivo()).isTrue();

    user.desativar();
    assertThat(user.isAtivo()).isFalse();

    user.ativar();
    assertThat(user.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Should test lombok methods")
  void shouldTestLombokMethods() {
    UUID id = UUID.randomUUID();
    Email email = Email.of("test@example.com");
    TestUser user = new TestUser(id, "Test", email, "pass", UserRole.ADMIN, true);

    assertThat(user.getId()).isEqualTo(id);
    assertThat(user.getNome()).isEqualTo("Test");
    assertThat(user.getEmail()).isEqualTo(email);
    assertThat(user.getPassword()).isEqualTo("pass");
    assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);

    TestUser user2 = new TestUser(id, "Test", email, "pass", UserRole.ADMIN, true);
    assertThat(user).isEqualTo(user2);
    assertThat(user.hashCode()).isEqualTo(user2.hashCode());

    assertThat(user.toString()).contains("id=" + id);
  }
}
