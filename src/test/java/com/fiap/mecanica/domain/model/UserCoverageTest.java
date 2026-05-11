package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.valueobject.*;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserCoverageTest {

  @Test
  @DisplayName("Deve cobrir métodos de User")
  void shouldCoverUserMethods() {
    // Usando Cliente como implementação concreta de User
    Cliente user =
        new Cliente(
            "Teste",
            CPF.of("529.982.247-25"),
            TipoPessoa.FISICA,
            Email.of("teste@teste.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));

    user.desativar();
    assertThat(user.isAtivo()).isFalse();

    user.ativar();
    assertThat(user.isAtivo()).isTrue();

    user.setPassword("123456");
    assertThat(user.getPassword()).isEqualTo("123456");

    user.setRole(UserRole.CLIENTE);
    assertThat(user.getRole()).isEqualTo(UserRole.CLIENTE);
  }

  @Test
  @DisplayName("Deve cobrir equals e hashCode")
  void shouldCoverEqualsAndHashCode() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Cliente c1 = new Cliente();
    c1.setId(id1);

    Cliente c2 = new Cliente();
    c2.setId(id1);

    Cliente c3 = new Cliente();
    c3.setId(id2);

    assertThat(c1).isEqualTo(c2);
    assertThat(c1).isNotEqualTo(c3);
    assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    assertThat(c1.hashCode()).isNotEqualTo(c3.hashCode());
    assertThat(c1).isNotEqualTo(null);
    assertThat(c1).isNotEqualTo(new Object());
  }

  @Test
  @DisplayName("Deve cobrir toString")
  void shouldCoverToString() {
    Cliente c = new Cliente();
    c.setId(UUID.randomUUID());
    c.setNome("Nome");
    assertThat(c.toString()).contains("Nome");
  }
}
