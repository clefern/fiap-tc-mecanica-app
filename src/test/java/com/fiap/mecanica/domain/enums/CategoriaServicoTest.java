package com.fiap.mecanica.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CategoriaServicoTest {

  @Test
  @DisplayName("Deve retornar descrição correta")
  void shouldReturnCorrectDescription() {
    assertThat(CategoriaServico.MANUTENCAO_PREVENTIVA.getDescricao())
        .isEqualTo("Manutenção Preventiva");
    assertThat(CategoriaServico.REPARO_MECANICO.getDescricao()).isEqualTo("Reparo Mecânico");
    assertThat(CategoriaServico.ELETRICA.getDescricao()).isEqualTo("Elétrica");
    assertThat(CategoriaServico.DIAGNOSTICO.getDescricao()).isEqualTo("Diagnóstico");
    assertThat(CategoriaServico.ESTETICA.getDescricao()).isEqualTo("Estética");
    assertThat(CategoriaServico.OUTROS.getDescricao()).isEqualTo("Outros");
  }

  @Test
  @DisplayName("Deve conter todos os valores")
  void shouldContainAllValues() {
    assertThat(CategoriaServico.values()).hasSize(6);
    assertThat(CategoriaServico.valueOf("MANUTENCAO_PREVENTIVA"))
        .isEqualTo(CategoriaServico.MANUTENCAO_PREVENTIVA);
  }
}
