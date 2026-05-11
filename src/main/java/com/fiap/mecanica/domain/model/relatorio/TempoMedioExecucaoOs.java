package com.fiap.mecanica.domain.model.relatorio;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempoMedioExecucaoOs {
  // Campo ID adicionado para compatibilidade com frameworks de teste/builder
  private String id;
  private LocalDateTime geradoEm;
  private Long quantidadeOsConsideradas;
  private Duration tempoMedioExecucao;
  private Duration tempoMinimoExecucao;
  private Duration tempoMaximoExecucao;

  @JsonProperty("tempoMedioExecucaoDescritivo")
  public String getTempoMedioExecucaoDescritivo() {
    return formatarDuracao(tempoMedioExecucao);
  }

  @JsonProperty("tempoMinimoExecucaoDescritivo")
  public String getTempoMinimoExecucaoDescritivo() {
    return formatarDuracao(tempoMinimoExecucao);
  }

  @JsonProperty("tempoMaximoExecucaoDescritivo")
  public String getTempoMaximoExecucaoDescritivo() {
    return formatarDuracao(tempoMaximoExecucao);
  }

  private String formatarDuracao(Duration duration) {
    if (duration == null || duration.isZero()) {
      return "0 segundos";
    }

    long dias = duration.toDays();
    long horas = duration.toHoursPart();
    long minutos = duration.toMinutesPart();
    long segundos = duration.toSecondsPart();

    List<String> partes = new ArrayList<>();
    if (dias > 0) {
      partes.add(dias + (dias == 1 ? " dia" : " dias"));
    }
    if (horas > 0) {
      partes.add(horas + (horas == 1 ? " hora" : " horas"));
    }
    if (minutos > 0) {
      partes.add(minutos + (minutos == 1 ? " minuto" : " minutos"));
    }
    if (segundos > 0) {
      partes.add(segundos + (segundos == 1 ? " segundo" : " segundos"));
    }

    if (partes.isEmpty()) {
      return "menos de 1 segundo";
    }

    if (partes.size() == 1) {
      return partes.getFirst();
    }

    String ultimo = partes.removeLast();
    return String.join(", ", partes) + " e " + ultimo;
  }
}
