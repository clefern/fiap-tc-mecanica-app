package com.fiap.mecanica.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CPFTest {

  @Test
  @DisplayName("Deve aceitar CPF válido (somente dígitos)")
  void shouldAcceptValidCpfDigitsOnly() {
    CPF cpf = CPF.of("52998224725");
    assertThat(cpf.valor()).isEqualTo("52998224725");
  }

  @Test
  @DisplayName("Deve aceitar CPF válido (formatado)")
  void shouldAcceptValidCpfFormatted() {
    CPF cpf = CPF.of("529.982.247-25");
    assertThat(cpf.valor()).isEqualTo("52998224725");
  }

  @Test
  @DisplayName("Não deve aceitar CPF inválido (dígitos repetidos)")
  void shouldRejectRepeatedDigitsCpf() {
    assertThrows(IllegalArgumentException.class, () -> CPF.of("11111111111"));
  }

  @Test
  @DisplayName("Não deve aceitar CPF inválido (dígitos aleatórios)")
  void shouldRejectRandomCpf() {
    assertThrows(IllegalArgumentException.class, () -> CPF.of("12345678900"));
  }

  @Test
  @DisplayName("Não deve aceitar CPF com tamanho incorreto")
  void shouldRejectWrongLength() {
    assertThrows(IllegalArgumentException.class, () -> CPF.of("5299822472"));
    assertThrows(IllegalArgumentException.class, () -> CPF.of("529982247252"));
  }

  @Test
  @DisplayName("Não deve aceitar CPF nulo ou vazio")
  void shouldRejectNullOrEmpty() {
    assertThrows(IllegalArgumentException.class, () -> CPF.of(null));
    assertThrows(IllegalArgumentException.class, () -> CPF.of(""));
    assertThrows(IllegalArgumentException.class, () -> CPF.of("   "));
  }

  @Test
  @DisplayName("Deve formatar CPF corretamente")
  void shouldFormatCorrectly() {
    CPF cpf = CPF.of("52998224725");
    assertThat(cpf.formatado()).isEqualTo("529.982.247-25");
  }

  @Test
  @DisplayName("Deve retornar valor no toString")
  void shouldReturnValorInToString() {
    CPF cpf = CPF.of("52998224725");
    assertThat(cpf.toString()).isEqualTo("52998224725");
  }

  @Test
  @DisplayName("equals/hashCode/toString devem considerar CPF normalizado")
  void equalsHashCodeAndToString() {
    CPF c1 = CPF.of("529.982.247-25");
    CPF c2 = CPF.of("52998224725");

    assertThat(c1).isEqualTo(c2);
    assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    assertThat(c1.toString()).isEqualTo("52998224725");
  }

  @Test
  @DisplayName("Deve aceitar CPF com caracteres não numéricos misturados (normalização)")
  void shouldNormalizeMixedChars() {
    CPF cpf = CPF.of("529a982b247c25");
    assertThat(cpf.valor()).isEqualTo("52998224725");
  }
}
