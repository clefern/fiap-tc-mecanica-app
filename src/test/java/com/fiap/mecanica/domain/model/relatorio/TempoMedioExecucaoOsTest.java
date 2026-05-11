package com.fiap.mecanica.domain.model.relatorio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TempoMedioExecucaoOsTest {

  @Test
  @DisplayName("Deve formatar duração com dias, horas, minutos e segundos")
  void shouldFormatDurationFull() {
    Duration duration = Duration.ofDays(4).plusHours(3).plusMinutes(23).plusSeconds(24);

    TempoMedioExecucaoOs model =
        TempoMedioExecucaoOs.builder().tempoMedioExecucao(duration).build();

    assertThat(model.getTempoMedioExecucaoDescritivo())
        .isEqualTo("4 dias, 3 horas, 23 minutos e 24 segundos");
  }

  @Test
  @DisplayName("Deve formatar duração no singular")
  void shouldFormatDurationSingular() {
    Duration duration = Duration.ofDays(1).plusHours(1).plusMinutes(1).plusSeconds(1);

    TempoMedioExecucaoOs model =
        TempoMedioExecucaoOs.builder().tempoMedioExecucao(duration).build();

    assertThat(model.getTempoMedioExecucaoDescritivo())
        .isEqualTo("1 dia, 1 hora, 1 minuto e 1 segundo");
  }

  @Test
  @DisplayName("Deve omitir partes zeradas")
  void shouldOmitZeroParts() {
    Duration duration = Duration.ofDays(2).plusMinutes(5);

    TempoMedioExecucaoOs model =
        TempoMedioExecucaoOs.builder().tempoMedioExecucao(duration).build();

    assertThat(model.getTempoMedioExecucaoDescritivo()).isEqualTo("2 dias e 5 minutos");
  }

  @Test
  @DisplayName("Deve retornar '0 segundos' para duração nula ou zero")
  void shouldReturnZeroSeconds() {
    TempoMedioExecucaoOs modelNull =
        TempoMedioExecucaoOs.builder().tempoMedioExecucao(null).build();
    assertThat(modelNull.getTempoMedioExecucaoDescritivo()).isEqualTo("0 segundos");

    TempoMedioExecucaoOs modelZero =
        TempoMedioExecucaoOs.builder().tempoMedioExecucao(Duration.ZERO).build();
    assertThat(modelZero.getTempoMedioExecucaoDescritivo()).isEqualTo("0 segundos");
  }

  @Test
  @DisplayName("Deve retornar 'menos de 1 segundo' para millis")
  void shouldReturnLessThanOneSecond() {
    Duration duration = Duration.ofMillis(500);

    TempoMedioExecucaoOs model =
        TempoMedioExecucaoOs.builder().tempoMedioExecucao(duration).build();

    assertThat(model.getTempoMedioExecucaoDescritivo()).isEqualTo("menos de 1 segundo");
  }

  @Test
  @DisplayName("Deve formatar corretamente tempo minimo e maximo")
  void shouldFormatMinAndMax() {
    TempoMedioExecucaoOs model =
        TempoMedioExecucaoOs.builder()
            .tempoMinimoExecucao(Duration.ofMinutes(10))
            .tempoMaximoExecucao(Duration.ofHours(2))
            .build();

    assertThat(model.getTempoMinimoExecucaoDescritivo()).isEqualTo("10 minutos");
    assertThat(model.getTempoMaximoExecucaoDescritivo()).isEqualTo("2 horas");
  }
}
