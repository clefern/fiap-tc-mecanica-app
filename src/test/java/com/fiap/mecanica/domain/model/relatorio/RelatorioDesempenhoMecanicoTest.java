package com.fiap.mecanica.domain.model.relatorio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RelatorioDesempenhoMecanicoTest {

  @Test
  @DisplayName("Should create RelatorioDesempenhoMecanico correctly")
  void shouldCreateRelatorioDesempenhoMecanicoCorrectly() {
    UUID id = UUID.randomUUID();
    String nome = "Mecanico Teste";
    Long qtd = 10L;
    BigDecimal receita = BigDecimal.valueOf(1000);
    Duration tempo = Duration.ofHours(2);

    RelatorioDesempenhoMecanico relatorio =
        new RelatorioDesempenhoMecanico(id, nome, qtd, receita, tempo);

    assertThat(relatorio.getMecanicoId()).isEqualTo(id);
    assertThat(relatorio.getNomeMecanico()).isEqualTo(nome);
    assertThat(relatorio.getQuantidadeOsConcluidas()).isEqualTo(qtd);
    assertThat(relatorio.getReceitaTotal()).isEqualTo(receita);
    assertThat(relatorio.getTempoMedioConclusao()).isEqualTo(tempo);

    RelatorioDesempenhoMecanico builderRelatorio =
        RelatorioDesempenhoMecanico.builder()
            .mecanicoId(id)
            .nomeMecanico(nome)
            .quantidadeOsConcluidas(qtd)
            .receitaTotal(receita)
            .tempoMedioConclusao(tempo)
            .build();

    assertThat(builderRelatorio).isEqualTo(relatorio);
    assertThat(builderRelatorio.hashCode()).isEqualTo(relatorio.hashCode());
    assertThat(builderRelatorio.toString()).contains(nome);
  }

  @Test
  @DisplayName("Should test no args constructor")
  void shouldTestNoArgsConstructor() {
    RelatorioDesempenhoMecanico relatorio = new RelatorioDesempenhoMecanico();
    assertThat(relatorio).isNotNull();
  }
}
