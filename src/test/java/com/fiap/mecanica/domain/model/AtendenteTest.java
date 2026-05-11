package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AtendenteTest {

  @Test
  @DisplayName("Deve criar atendente com CPF e Email válidos")
  void shouldCreateAtendenteWithValidVOs() {
    Atendente a =
        new Atendente("Maria Atendente", CPF.of("529.982.247-25"), Email.of("maria@oficina.com"));
    assertThat(a.getNome()).isEqualTo("Maria Atendente");
    assertThat(a.getCpf().valor()).isEqualTo("52998224725");
    assertThat(a.getEmail().value()).isEqualTo("maria@oficina.com");
    assertThat(a.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Não deve criar atendente com nome vazio ou nulo")
  void shouldRejectEmptyName() {
    CPF cpf = CPF.of("529.982.247-25");
    Email email = Email.of("maria@oficina.com");

    assertThrows(IllegalArgumentException.class, () -> new Atendente(null, cpf, email));
    assertThrows(IllegalArgumentException.class, () -> new Atendente("", cpf, email));
    assertThrows(IllegalArgumentException.class, () -> new Atendente("   ", cpf, email));
  }

  @Test
  @DisplayName("Deve permitir alterar status ativo")
  void shouldToggleAtivo() {
    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));
    assertThat(a.isAtivo()).isTrue();

    a.setAtivo(false);
    assertThat(a.isAtivo()).isFalse();

    a.setAtivo(true);
    assertThat(a.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Deve permitir atualizar dados")
  void shouldUpdateData() {
    Atendente a = new Atendente("Maria", CPF.of("529.982.247-25"), Email.of("m@o.com"));

    a.setNome("Maria Silva");
    a.setEmail(Email.of("maria.silva@oficina.com"));

    assertThat(a.getNome()).isEqualTo("Maria Silva");
    assertThat(a.getEmail().value()).isEqualTo("maria.silva@oficina.com");
  }
}
