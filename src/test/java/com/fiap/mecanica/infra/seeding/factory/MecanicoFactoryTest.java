package com.fiap.mecanica.infra.seeding.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.model.Mecanico;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MecanicoFactoryTest {

  private final MecanicoFactory factory = new MecanicoFactory();

  @Test
  @DisplayName("Should create single mechanic with valid data")
  void shouldCreateSingleMechanic() {
    Mecanico mecanico = factory.create();

    assertThat(mecanico).isNotNull();
    // ID and Password are not set by the factory/constructor used
    assertThat(mecanico.getNome()).isNotBlank();
    assertThat(mecanico.getEmail()).isNotNull();
    assertThat(mecanico.getEspecialidade()).isNotBlank();
    assertThat(mecanico.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Should create multiple mechanics")
  void shouldCreateMultipleMechanics() {
    int count = 5;
    List<Mecanico> mecanicos = factory.createMany(count);

    assertThat(mecanicos).hasSize(count);
    mecanicos.forEach(
        m -> {
          assertThat(m).isNotNull();
          assertThat(m.getNome()).isNotBlank();
        });
  }
}
