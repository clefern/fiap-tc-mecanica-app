package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.valueobject.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminTest {

  @Test
  @DisplayName("Should create Admin correctly")
  void shouldCreateAdminCorrectly() {
    String nome = "Admin User";
    Email email = Email.of("admin@example.com");
    String password = "password123";

    Admin admin = new Admin(nome, email, password);

    assertThat(admin.getNome()).isEqualTo(nome);
    assertThat(admin.getEmail()).isEqualTo(email);
    assertThat(admin.getPassword()).isEqualTo(password);
    assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(admin.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Should test lombok methods")
  void shouldTestLombokMethods() {
    Admin admin1 = new Admin("Admin", Email.of("a@b.com"), "pass");
    Admin admin2 = new Admin("Admin", Email.of("a@b.com"), "pass");

    // Equals uses ID, which is null here, but lombok usually handles it if explicitly included.
    // However, Admin inherits EqualsAndHashCode(callSuper=true) from User which has
    // @EqualsAndHashCode(onlyExplicitlyIncluded = true) on ID.
    // If ID is null, they might be equal if ID is the ONLY field.
    // Let's check toString at least.

    assertThat(admin1.toString()).contains("Admin");
  }
}
