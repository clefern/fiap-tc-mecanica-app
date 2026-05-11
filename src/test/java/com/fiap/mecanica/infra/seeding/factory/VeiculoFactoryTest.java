package com.fiap.mecanica.infra.seeding.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.model.Veiculo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VeiculoFactoryTest {

  private final VeiculoFactory factory = new VeiculoFactory();

  @Test
  @DisplayName("Deve criar veículo válido")
  void shouldCreateVeiculo() {
    Veiculo v = factory.create();
    assertThat(v).isNotNull();
    assertThat(v.getPlaca()).isNotNull();
    assertThat(v.getModelo()).isNotBlank();
    assertThat(v.getMarca()).isNotBlank();
    assertThat(v.getAno()).isGreaterThan(1900);
  }

  @Test
  @DisplayName("Deve criar múltiplos veículos")
  void shouldCreateMany() {
    List<Veiculo> veiculos = factory.createMany(5);
    assertThat(veiculos).hasSize(5);
    veiculos.forEach(v -> assertThat(v).isNotNull());
  }
}
