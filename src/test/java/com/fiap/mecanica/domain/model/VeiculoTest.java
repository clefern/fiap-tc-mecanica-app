package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VeiculoTest {

  @Test
  @DisplayName("Deve criar veículo com dados válidos")
  void shouldCreateVeiculo() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1234");
    Veiculo veiculo = new Veiculo(placa, " Fusca ", " VW ", 2020);

    assertThat(veiculo.getPlaca()).isEqualTo(placa);
    assertThat(veiculo.getModelo()).isEqualTo("Fusca");
    assertThat(veiculo.getMarca()).isEqualTo("VW");
    assertThat(veiculo.getAno()).isEqualTo(2020);
  }

  @Test
  @DisplayName("Deve tratar modelo e marca nulos")
  void shouldHandleNullModeloAndMarca() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1234");
    Veiculo veiculo = new Veiculo(placa, null, null, 2020);

    assertThat(veiculo.getModelo()).isEmpty();
    assertThat(veiculo.getMarca()).isEmpty();
  }

  @Test
  @DisplayName("Deve lançar exceção para ano inválido")
  void shouldThrowForInvalidAno() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1234");

    // Too old
    assertThrows(IllegalArgumentException.class, () -> new Veiculo(placa, "M", "B", 1899));

    // Too future
    int nextYear = LocalDate.now().getYear() + 2;
    assertThrows(IllegalArgumentException.class, () -> new Veiculo(placa, "M", "B", nextYear));
  }

  @Test
  @DisplayName("Deve verificar igualdade pela placa")
  void shouldVerifyEquality() {
    PlacaVeiculo p1 = PlacaVeiculo.of("ABC1234");
    PlacaVeiculo p2 = PlacaVeiculo.of("XYZ9876");

    Veiculo v1 = new Veiculo(p1, "M", "B", 2020);
    Veiculo v2 = new Veiculo(p1, "X", "Y", 2021); // Same placa, diff other fields
    Veiculo v3 = new Veiculo(p2, "M", "B", 2020);

    assertThat(v1).isEqualTo(v2);
    assertThat(v1).isNotEqualTo(v3);
    assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
  }

  @Test
  @DisplayName("Deve verificar se veículo é antigo")
  void shouldCheckIsAntigo() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1234");
    int currentYear = LocalDate.now().getYear();

    Veiculo novo = new Veiculo(placa, "M", "B", currentYear);
    assertThat(novo.isAntigo()).isFalse();

    Veiculo antigo = new Veiculo(placa, "M", "B", currentYear - 21);
    assertThat(antigo.isAntigo()).isTrue();
  }

  @Test
  @DisplayName("Deve lançar exceção para placa nula")
  void shouldThrowForNullPlaca() {
    assertThrows(NullPointerException.class, () -> new Veiculo(null, "M", "B", 2020));
  }

  @Test
  @DisplayName("Deve permitir alterar ID")
  void shouldSetId() {
    Veiculo v = new Veiculo(PlacaVeiculo.of("ABC1234"), "M", "B", 2020);
    UUID id = UUID.randomUUID();
    v.setId(id);
    assertThat(v.getId()).isEqualTo(id);
  }

  @Test
  @DisplayName("Deve retornar string representativa")
  void shouldReturnToString() {
    Veiculo v = new Veiculo(PlacaVeiculo.of("ABC1234"), "Fusca", "VW", 2020);
    assertThat(v.toString()).contains("ABC1234", "Fusca", "VW", "2020");
  }
}
