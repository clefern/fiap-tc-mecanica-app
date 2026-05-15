package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.RelatorioService;
import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import com.fiap.mecanica.domain.repository.RelatorioRepository;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import java.time.LocalDate;
import java.util.List;

import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RelatorioServiceImpl implements RelatorioService {

  private final RelatorioRepository relatorioRepository;

  public RelatorioServiceImpl(RelatorioRepository relatorioRepository) {
    this.relatorioRepository = relatorioRepository;
  }

  @Override
  @Transactional(readOnly = true)
  @MonitoredOperation(type = MonitoredOperationType.REPORT_MECHANIC)
  @MonitoredOperation(type = MonitoredOperationType.MECHANIC_PERFORMANCE)
  public List<RelatorioDesempenhoMecanico> gerarRelatorioDesempenho(
      LocalDate inicio, LocalDate fim) {
    log.info("[RELATORIO_DESEMPENHO_MECANICO] inicio={} fim={}", inicio, fim);
    if (inicio == null) {
      inicio = LocalDate.now().minusMonths(1);
    }
    if (fim == null) {
      fim = LocalDate.now();
    }
    if (inicio.isAfter(fim)) {
      throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
    }

    return relatorioRepository.gerarRelatorioDesempenho(inicio, fim);
  }

  @Override
  @Transactional(readOnly = true)
  @MonitoredOperation(type = MonitoredOperationType.REPORT_OS_AVERAGE_EXECUTION_TIME)
  public TempoMedioExecucaoOs obterTempoMedioExecucaoOs() {
    log.info("[RELATORIO_TEMPO_MEDIO_OS_GLOBAL]");
    return relatorioRepository.calcularTempoMedioExecucaoOs();
  }

  @Override
  @Transactional(readOnly = true)
	@MonitoredOperation(type = MonitoredOperationType.REPORT_OS_AVERAGE_EXECUTION_TIME)
  public TempoMedioExecucaoOs obterTempoMedioExecucaoOsPorPeriodo(LocalDate inicio, LocalDate fim) {
    log.info("[RELATORIO_TEMPO_MEDIO_OS_PERIODO] inicio={} fim={}", inicio, fim);
    if (inicio == null || fim == null) {
      throw new IllegalArgumentException(
          "Data de início e data de fim são obrigatórias para o cálculo por período");
    }
    if (inicio.isAfter(fim)) {
      throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
    }

    return relatorioRepository.calcularTempoMedioExecucaoOsPorPeriodo(inicio, fim);
  }
}
