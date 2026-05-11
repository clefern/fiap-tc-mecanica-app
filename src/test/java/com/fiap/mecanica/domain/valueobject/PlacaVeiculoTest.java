package com.fiap.mecanica.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlacaVeiculoTest {

  @Test
  @DisplayName("Deve aceitar placa no padrão Mercosul e normalizar")
  void shouldAcceptMercosulPlate() {
    PlacaVeiculo placa = PlacaVeiculo.of("abc1d23");
    assertThat(placa.value()).isEqualTo("ABC1D23");
  }

  @Test
  @DisplayName("Deve aceitar placa legada e normalizar sem hífen")
  void shouldAcceptLegacyPlate() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC-1234");
    assertThat(placa.value()).isEqualTo("ABC1234");
  }

  @Test
  @DisplayName("Não deve aceitar placas inválidas")
  void shouldRejectInvalidPlates() {
    assertThrows(IllegalArgumentException.class, () -> PlacaVeiculo.of("ABCD123"));
    assertThrows(IllegalArgumentException.class, () -> PlacaVeiculo.of("A1B2C3D"));
    assertThrows(IllegalArgumentException.class, () -> PlacaVeiculo.of("123-ABCD"));
    assertThrows(IllegalArgumentException.class, () -> PlacaVeiculo.of(""));
    assertThrows(IllegalArgumentException.class, () -> PlacaVeiculo.of(null));
  }

  @Test
  @DisplayName("equals/hashCode/toString devem considerar placa normalizada")
  void equalsHashCodeAndToString() {
    PlacaVeiculo p1 = PlacaVeiculo.of("abc1d23");
    PlacaVeiculo p2 = PlacaVeiculo.of("ABC1D23");

    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    assertThat(p1.toString()).isEqualTo("ABC1D23");
  }
}
