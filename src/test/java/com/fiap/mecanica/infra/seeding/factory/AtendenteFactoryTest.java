package com.fiap.mecanica.infra.seeding.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.Atendente;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AtendenteFactoryTest {

  private final AtendenteFactory factory = new AtendenteFactory();

  @Test
  @DisplayName("Should create single attendant with valid data")
  void shouldCreateSingleAttendant() {
    Atendente atendente = factory.create();

    assertThat(atendente).isNotNull();
    assertThat(atendente.getNome()).isNotBlank();
    assertThat(atendente.getEmail()).isNotNull();
    assertThat(atendente.getCpf()).isNotNull();
    assertThat(atendente.isAtivo()).isTrue();
    assertThat(atendente.getRole()).isEqualTo(UserRole.ATENDENTE);
  }

  @Test
  @DisplayName("Should create multiple attendants")
  void shouldCreateMultipleAttendants() {
    int count = 5;
    List<Atendente> atendentes = factory.createMany(count);

    assertThat(atendentes).hasSize(count);
    atendentes.forEach(
        a -> {
          assertThat(a).isNotNull();
          assertThat(a.getNome()).isNotBlank();
          assertThat(a.getCpf()).isNotNull();
        });
  }
}
