package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.RelatorioService;
import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.RelatorioApi;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@SecurityRequirement(name = "bearerAuth")
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class RelatorioController implements RelatorioApi {

  private final RelatorioService relatorioService;

  public RelatorioController(RelatorioService relatorioService) {
    this.relatorioService = relatorioService;
  }

  @Override
  @GetMapping("/desempenho-mecanicos")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<List<RelatorioDesempenhoMecanico>> getDesempenhoMecanicos(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate inicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fim) {

    List<RelatorioDesempenhoMecanico> relatorio =
        relatorioService.gerarRelatorioDesempenho(inicio, fim);
    return ResponseEntity.ok(relatorio);
  }

  @Override
  @GetMapping("/tempo-medio-execucao-os")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<TempoMedioExecucaoOs> getTempoMedioExecucaoOs() {
    TempoMedioExecucaoOs relatorio = relatorioService.obterTempoMedioExecucaoOs();
    return ResponseEntity.ok(relatorio);
  }

  @Override
  @GetMapping("/tempo-medio-execucao-os/periodo")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<TempoMedioExecucaoOs> getTempoMedioExecucaoOsPorPeriodo(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

    TempoMedioExecucaoOs relatorio =
        relatorioService.obterTempoMedioExecucaoOsPorPeriodo(inicio, fim);
    return ResponseEntity.ok(relatorio);
  }
}
