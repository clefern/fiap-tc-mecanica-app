package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MecanicoTest {

  @Test
  @DisplayName("Deve criar mecânico com CPF, Email e Especialidade válidos")
  void shouldCreateMecanicoWithValidVOs() {
    Mecanico m =
        new Mecanico(
            "João Mecânico",
            CPF.of("529.982.247-25"),
            Email.of("joao@oficina.com"),
            "Mecânica de Motor");
    assertThat(m.getNome()).isEqualTo("João Mecânico");
    assertThat(m.getCpf().valor()).isEqualTo("52998224725");
    assertThat(m.getEmail().value()).isEqualTo("joao@oficina.com");
    assertThat(m.getEspecialidade()).isEqualTo("Mecânica de Motor");
    assertThat(m.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Não deve criar mecânico com nome vazio ou nulo")
  void shouldRejectEmptyName() {
    CPF cpf = CPF.of("529.982.247-25");
    Email email = Email.of("joao@oficina.com");

    assertThrows(
        IllegalArgumentException.class, () -> new Mecanico(null, cpf, email, "Mecânica de Motor"));
    assertThrows(
        IllegalArgumentException.class, () -> new Mecanico("", cpf, email, "Mecânica de Motor"));
    assertThrows(
        IllegalArgumentException.class, () -> new Mecanico("   ", cpf, email, "Mecânica de Motor"));
  }

  @Test
  @DisplayName("Não deve criar mecânico com especialidade vazia ou nula")
  void shouldRejectEmptyEspecialidade() {
    CPF cpf = CPF.of("529.982.247-25");
    Email email = Email.of("joao@oficina.com");

    assertThrows(IllegalArgumentException.class, () -> new Mecanico("João", cpf, email, null));
    assertThrows(IllegalArgumentException.class, () -> new Mecanico("João", cpf, email, ""));
    assertThrows(IllegalArgumentException.class, () -> new Mecanico("João", cpf, email, "   "));
  }

  @Test
  @DisplayName("Deve permitir alterar status ativo")
  void shouldToggleAtivo() {
    Mecanico m =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica Geral");
    assertThat(m.isAtivo()).isTrue();

    m.setAtivo(false);
    assertThat(m.isAtivo()).isFalse();

    m.setAtivo(true);
    assertThat(m.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Deve permitir atualizar dados")
  void shouldUpdateData() {
    Mecanico m =
        new Mecanico("João", CPF.of("529.982.247-25"), Email.of("j@o.com"), "Mecânica Geral");

    m.setNome("João Silva");
    m.setEspecialidade("Freios e ABS");
    m.setEmail(Email.of("joao.silva@oficina.com"));

    assertThat(m.getNome()).isEqualTo("João Silva");
    assertThat(m.getEspecialidade()).isEqualTo("Freios e ABS");
    assertThat(m.getEmail().value()).isEqualTo("joao.silva@oficina.com");
  }
}
