package com.fiap.mecanica.infra.repository;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import com.fiap.mecanica.domain.repository.RelatorioRepository;
import com.fiap.mecanica.infra.entity.MecanicoEntity;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import com.fiap.mecanica.infra.jpa.JpaMecanicoRepository;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class RelatorioRepositoryImpl implements RelatorioRepository {

  private final JpaOrdemServicoRepository osRepository;
  private final JpaMecanicoRepository mecanicoRepository;

  public RelatorioRepositoryImpl(
      JpaOrdemServicoRepository osRepository, JpaMecanicoRepository mecanicoRepository) {
    this.osRepository = osRepository;
    this.mecanicoRepository = mecanicoRepository;
  }

  @Override
  public List<RelatorioDesempenhoMecanico> gerarRelatorioDesempenho(
      LocalDate inicio, LocalDate fim) {
    LocalDateTime start = inicio.atStartOfDay();
    LocalDateTime end = fim.atTime(LocalTime.MAX);

    List<OrdemServicoEntity> ordens =
        osRepository.findByStatusAndDataFechamentoBetweenAndMecanicoExecucaoIdIsNotNull(
            StatusOS.FINALIZADA, start, end);

    // Group by MecanicoId
    Map<UUID, List<OrdemServicoEntity>> byMecanico =
        ordens.stream().collect(Collectors.groupingBy(OrdemServicoEntity::getMecanicoExecucaoId));

    List<RelatorioDesempenhoMecanico> relatorio = new ArrayList<>();

    // Fetch all mechanics involved
    List<MecanicoEntity> mecanicos = mecanicoRepository.findAllById(byMecanico.keySet());
    Map<UUID, String> mecanicoNomes =
        mecanicos.stream()
            .collect(Collectors.toMap(MecanicoEntity::getId, MecanicoEntity::getNome));

    for (Map.Entry<UUID, List<OrdemServicoEntity>> entry : byMecanico.entrySet()) {
      UUID mecanicoId = entry.getKey();
      List<OrdemServicoEntity> osList = entry.getValue();

      long count = osList.size();
      BigDecimal totalReceita =
          osList.stream()
              .map(OrdemServicoEntity::getValorTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      Duration totalDuration = Duration.ZERO;
      long validDurationCount = 0;

      for (OrdemServicoEntity os : osList) {
        if (os.getDataEntrada() != null && os.getDataFechamento() != null) {
          totalDuration =
              totalDuration.plus(Duration.between(os.getDataEntrada(), os.getDataFechamento()));
          validDurationCount++;
        }
      }

      Duration avgDuration =
          validDurationCount > 0 ? totalDuration.dividedBy(validDurationCount) : Duration.ZERO;

      String nome = mecanicoNomes.getOrDefault(mecanicoId, "Desconhecido");

      relatorio.add(
          new RelatorioDesempenhoMecanico(mecanicoId, nome, count, totalReceita, avgDuration));
    }

    return relatorio;
  }

  @Override
  public TempoMedioExecucaoOs calcularTempoMedioExecucaoOs() {
    List<OrdemServicoEntity> ordens =
        osRepository.findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(
            StatusOS.FINALIZADA);

    return calcularTempoMedioExecucao(ordens);
  }

  @Override
  public TempoMedioExecucaoOs calcularTempoMedioExecucaoOsPorPeriodo(
      LocalDate inicio, LocalDate fim) {
    List<OrdemServicoEntity> ordens =
        osRepository.findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(
            StatusOS.FINALIZADA);

    List<OrdemServicoEntity> filtradas =
        ordens.stream()
            .filter(
                os -> {
                  if (os.getDataFechamento() == null) {
                    return false;
                  }
                  LocalDate dataFechamento = os.getDataFechamento().toLocalDate();
                  return !dataFechamento.isBefore(inicio) && !dataFechamento.isAfter(fim);
                })
            .toList();

    return calcularTempoMedioExecucao(filtradas);
  }

  private TempoMedioExecucaoOs calcularTempoMedioExecucao(List<OrdemServicoEntity> ordens) {
    if (ordens.isEmpty()) {
      return TempoMedioExecucaoOs.builder()
          .geradoEm(null)
          .quantidadeOsConsideradas(0L)
          .tempoMedioExecucao(Duration.ZERO)
          .tempoMinimoExecucao(Duration.ZERO)
          .tempoMaximoExecucao(Duration.ZERO)
          .build();
    }

    Duration totalDuration = Duration.ZERO;
    Duration minDuration = null;
    Duration maxDuration = null;
    long count = 0;

    for (OrdemServicoEntity os : ordens) {
      if (os.getDataAprovacao() != null && os.getDataFechamento() != null) {
        Duration duration = Duration.between(os.getDataAprovacao(), os.getDataFechamento());
        totalDuration = totalDuration.plus(duration);
        if (minDuration == null || duration.compareTo(minDuration) < 0) {
          minDuration = duration;
        }
        if (maxDuration == null || duration.compareTo(maxDuration) > 0) {
          maxDuration = duration;
        }
        count++;
      }
    }

    if (count == 0) {
      return TempoMedioExecucaoOs.builder()
          .geradoEm(null)
          .quantidadeOsConsideradas(0L)
          .tempoMedioExecucao(Duration.ZERO)
          .tempoMinimoExecucao(Duration.ZERO)
          .tempoMaximoExecucao(Duration.ZERO)
          .build();
    }

    Duration avgDuration = totalDuration.dividedBy(count);

    return TempoMedioExecucaoOs.builder()
        .geradoEm(LocalDateTime.now())
        .quantidadeOsConsideradas(count)
        .tempoMedioExecucao(avgDuration)
        .tempoMinimoExecucao(minDuration)
        .tempoMaximoExecucao(maxDuration)
        .build();
  }
}
