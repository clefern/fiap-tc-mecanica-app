package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import java.time.LocalDate;
import java.util.List;

public interface RelatorioRepository extends BaseRepository {
  List<RelatorioDesempenhoMecanico> gerarRelatorioDesempenho(LocalDate inicio, LocalDate fim);

  TempoMedioExecucaoOs calcularTempoMedioExecucaoOs();

  TempoMedioExecucaoOs calcularTempoMedioExecucaoOsPorPeriodo(LocalDate inicio, LocalDate fim);
}
