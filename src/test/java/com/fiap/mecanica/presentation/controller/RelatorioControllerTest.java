package com.fiap.mecanica.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fiap.mecanica.application.service.RelatorioService;
import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RelatorioControllerTest {

  private MockMvc mockMvc;

  @Mock private RelatorioService relatorioService;

  @InjectMocks private RelatorioController relatorioController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(relatorioController).build();
  }

  @Test
  @DisplayName("Deve obter relatório de desempenho dos mecânicos")
  void shouldGetDesempenhoMecanicos() throws Exception {
    RelatorioDesempenhoMecanico relatorio =
        RelatorioDesempenhoMecanico.builder()
            .mecanicoId(UUID.randomUUID())
            .nomeMecanico("Mecanico Teste")
            .quantidadeOsConcluidas(10L)
            .receitaTotal(new BigDecimal("1500.00"))
            .tempoMedioConclusao(Duration.ofHours(2))
            .build();

    when(relatorioService.gerarRelatorioDesempenho(any(), any()))
        .thenReturn(Collections.singletonList(relatorio));

    mockMvc
        .perform(
            get("/api/relatorios/desempenho-mecanicos")
                .param("inicio", LocalDate.now().minusDays(30).toString())
                .param("fim", LocalDate.now().toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].nomeMecanico").value("Mecanico Teste"))
        .andExpect(jsonPath("$[0].quantidadeOsConcluidas").value(10))
        .andExpect(jsonPath("$[0].receitaTotal").value(1500.00));
  }

  @Test
  @DisplayName("Deve obter relatório de desempenho dos mecânicos sem datas")
  void shouldGetDesempenhoMecanicosWithoutDates() throws Exception {
    when(relatorioService.gerarRelatorioDesempenho(null, null)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            get("/api/relatorios/desempenho-mecanicos").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @DisplayName("Deve obter tempo médio de execução de OS")
  void shouldGetTempoMedioExecucaoOs() throws Exception {
    TempoMedioExecucaoOs relatorio =
        TempoMedioExecucaoOs.builder()
            .quantidadeOsConsideradas(50L)
            .tempoMedioExecucao(Duration.ofDays(1).plusHours(2))
            .tempoMinimoExecucao(Duration.ofHours(1))
            .tempoMaximoExecucao(Duration.ofDays(3))
            .build();

    when(relatorioService.obterTempoMedioExecucaoOs()).thenReturn(relatorio);

    mockMvc
        .perform(
            get("/api/relatorios/tempo-medio-execucao-os").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantidadeOsConsideradas").value(50))
        .andExpect(jsonPath("$.tempoMedioExecucaoDescritivo").exists());
  }

  @Test
  @DisplayName("Deve obter tempo médio de execução de OS por período")
  void shouldGetTempoMedioExecucaoOsPorPeriodo() throws Exception {
    LocalDate inicio = LocalDate.now().minusDays(10);
    LocalDate fim = LocalDate.now();

    TempoMedioExecucaoOs relatorio =
        TempoMedioExecucaoOs.builder()
            .quantidadeOsConsideradas(20L)
            .tempoMedioExecucao(Duration.ofHours(5))
            .build();

    when(relatorioService.obterTempoMedioExecucaoOsPorPeriodo(eq(inicio), eq(fim)))
        .thenReturn(relatorio);

    mockMvc
        .perform(
            get("/api/relatorios/tempo-medio-execucao-os/periodo")
                .param("inicio", inicio.toString())
                .param("fim", fim.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantidadeOsConsideradas").value(20));
  }
}
