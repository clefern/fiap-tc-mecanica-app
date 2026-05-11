package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import java.time.LocalDate;
import java.util.List;

public interface RelatorioService {
  List<RelatorioDesempenhoMecanico> gerarRelatorioDesempenho(LocalDate inicio, LocalDate fim);

  TempoMedioExecucaoOs obterTempoMedioExecucaoOs();

  TempoMedioExecucaoOs obterTempoMedioExecucaoOsPorPeriodo(LocalDate inicio, LocalDate fim);
}
