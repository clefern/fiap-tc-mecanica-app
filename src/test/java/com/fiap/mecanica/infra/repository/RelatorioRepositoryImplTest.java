package com.fiap.mecanica.infra.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import com.fiap.mecanica.infra.entity.MecanicoEntity;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import com.fiap.mecanica.infra.jpa.JpaMecanicoRepository;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelatorioRepositoryImplTest {

  @Test
  @DisplayName("Deve retornar zero quando não houver OS finalizada")
  void shouldReturnZeroWhenNoOrders() {
    JpaOrdemServicoRepository osRepository = mock(JpaOrdemServicoRepository.class);
    JpaMecanicoRepository mecanicoRepository = mock(JpaMecanicoRepository.class);
    RelatorioRepositoryImpl repository =
        new RelatorioRepositoryImpl(osRepository, mecanicoRepository);
    when(osRepository.findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(
            StatusOS.FINALIZADA))
        .thenReturn(Collections.emptyList());

    TempoMedioExecucaoOs resultado = repository.calcularTempoMedioExecucaoOs();

    assertThat(resultado.getQuantidadeOsConsideradas()).isZero();
    assertThat(resultado.getTempoMedioExecucao()).isEqualTo(Duration.ZERO);
    assertThat(resultado.getTempoMinimoExecucao()).isEqualTo(Duration.ZERO);
    assertThat(resultado.getTempoMaximoExecucao()).isEqualTo(Duration.ZERO);
  }

  @Test
  @DisplayName("Deve calcular tempo médio de execução entre aprovação e fechamento")
  void shouldCalculateAverageExecutionTime() {
    JpaOrdemServicoRepository osRepository = mock(JpaOrdemServicoRepository.class);
    JpaMecanicoRepository mecanicoRepository = mock(JpaMecanicoRepository.class);
    RelatorioRepositoryImpl repository =
        new RelatorioRepositoryImpl(osRepository, mecanicoRepository);
    LocalDateTime aprovacao = LocalDateTime.now().minusHours(2);
    LocalDateTime fechamento = aprovacao.plusHours(2);

    OrdemServicoEntity os1 = new OrdemServicoEntity();
    os1.setId(UUID.randomUUID());
    os1.setStatus(StatusOS.FINALIZADA);
    os1.setDataAprovacao(aprovacao);
    os1.setDataFechamento(fechamento);

    OrdemServicoEntity os2 = new OrdemServicoEntity();
    os2.setId(UUID.randomUUID());
    os2.setStatus(StatusOS.FINALIZADA);
    os2.setDataAprovacao(aprovacao);
    os2.setDataFechamento(fechamento);

    when(osRepository.findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(
            StatusOS.FINALIZADA))
        .thenReturn(List.of(os1, os2));

    TempoMedioExecucaoOs resultado = repository.calcularTempoMedioExecucaoOs();

    assertThat(resultado.getQuantidadeOsConsideradas()).isEqualTo(2L);
    assertThat(resultado.getTempoMedioExecucao()).isEqualTo(Duration.ofHours(2));
    assertThat(resultado.getTempoMinimoExecucao()).isEqualTo(Duration.ofHours(2));
    assertThat(resultado.getTempoMaximoExecucao()).isEqualTo(Duration.ofHours(2));
    assertThat(resultado.getGeradoEm()).isNotNull();
  }

  @Test
  @DisplayName(
      "Deve retornar zero quando ordens não possuem datas de aprovação e fechamento válidas")
  void shouldReturnZeroWhenOrdersHaveNoValidApprovalAndClosingDates() {
    JpaOrdemServicoRepository osRepository = mock(JpaOrdemServicoRepository.class);
    JpaMecanicoRepository mecanicoRepository = mock(JpaMecanicoRepository.class);
    RelatorioRepositoryImpl repository =
        new RelatorioRepositoryImpl(osRepository, mecanicoRepository);
    OrdemServicoEntity os1 = new OrdemServicoEntity();
    os1.setId(UUID.randomUUID());
    os1.setStatus(StatusOS.FINALIZADA);
    os1.setDataAprovacao(null);
    os1.setDataFechamento(LocalDateTime.now());

    OrdemServicoEntity os2 = new OrdemServicoEntity();
    os2.setId(UUID.randomUUID());
    os2.setStatus(StatusOS.FINALIZADA);
    os2.setDataAprovacao(LocalDateTime.now());
    os2.setDataFechamento(null);

    when(osRepository.findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(
            StatusOS.FINALIZADA))
        .thenReturn(List.of(os1, os2));

    TempoMedioExecucaoOs resultado = repository.calcularTempoMedioExecucaoOs();

    assertThat(resultado.getQuantidadeOsConsideradas()).isZero();
    assertThat(resultado.getTempoMedioExecucao()).isEqualTo(Duration.ZERO);
    assertThat(resultado.getTempoMinimoExecucao()).isEqualTo(Duration.ZERO);
    assertThat(resultado.getTempoMaximoExecucao()).isEqualTo(Duration.ZERO);
  }

  @Test
  @DisplayName("Deve calcular tempo médio de execução por período considerando data de fechamento")
  void shouldCalculateAverageExecutionTimeByPeriodUsingClosingDate() {
    JpaOrdemServicoRepository osRepository = mock(JpaOrdemServicoRepository.class);
    JpaMecanicoRepository mecanicoRepository = mock(JpaMecanicoRepository.class);
    RelatorioRepositoryImpl repository =
        new RelatorioRepositoryImpl(osRepository, mecanicoRepository);
    LocalDateTime base = LocalDateTime.now();

    LocalDateTime aprovacaoDentro = base.minusHours(2);
    LocalDateTime fechamentoDentro = base;

    LocalDateTime aprovacaoFora = base.minusDays(10);
    LocalDateTime fechamentoFora = base.minusDays(10);

    OrdemServicoEntity osDentro = new OrdemServicoEntity();
    osDentro.setId(UUID.randomUUID());
    osDentro.setStatus(StatusOS.FINALIZADA);
    osDentro.setDataAprovacao(aprovacaoDentro);
    osDentro.setDataFechamento(fechamentoDentro);

    OrdemServicoEntity osFora = new OrdemServicoEntity();
    osFora.setId(UUID.randomUUID());
    osFora.setStatus(StatusOS.FINALIZADA);
    osFora.setDataAprovacao(aprovacaoFora);
    osFora.setDataFechamento(fechamentoFora);

    when(osRepository.findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(
            StatusOS.FINALIZADA))
        .thenReturn(List.of(osDentro, osFora));

    LocalDate inicio = base.toLocalDate().minusDays(1);
    LocalDate fim = base.toLocalDate().plusDays(1);

    TempoMedioExecucaoOs resultado = repository.calcularTempoMedioExecucaoOsPorPeriodo(inicio, fim);

    assertThat(resultado.getQuantidadeOsConsideradas()).isEqualTo(1L);
    assertThat(resultado.getTempoMedioExecucao())
        .isEqualTo(Duration.between(aprovacaoDentro, fechamentoDentro));
  }

  @Test
  @DisplayName("Deve ignorar OS com data de fechamento nula ao calcular por período")
  void shouldIgnoreOrdersWithNullClosingDateWhenCalculatingByPeriod() {
    JpaOrdemServicoRepository osRepository = mock(JpaOrdemServicoRepository.class);
    JpaMecanicoRepository mecanicoRepository = mock(JpaMecanicoRepository.class);
    RelatorioRepositoryImpl repository =
        new RelatorioRepositoryImpl(osRepository, mecanicoRepository);
    LocalDateTime base = LocalDateTime.now();

    OrdemServicoEntity osSemFechamento = new OrdemServicoEntity();
    osSemFechamento.setId(UUID.randomUUID());
    osSemFechamento.setStatus(StatusOS.FINALIZADA);
    osSemFechamento.setDataAprovacao(base.minusHours(3));
    osSemFechamento.setDataFechamento(null);

    OrdemServicoEntity osValida = new OrdemServicoEntity();
    osValida.setId(UUID.randomUUID());
    osValida.setStatus(StatusOS.FINALIZADA);
    osValida.setDataAprovacao(base.minusHours(2));
    osValida.setDataFechamento(base);

    when(osRepository.findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(
            StatusOS.FINALIZADA))
        .thenReturn(List.of(osSemFechamento, osValida));

    LocalDate inicio = base.toLocalDate().minusDays(1);
    LocalDate fim = base.toLocalDate().plusDays(1);

    TempoMedioExecucaoOs resultado = repository.calcularTempoMedioExecucaoOsPorPeriodo(inicio, fim);

    assertThat(resultado.getQuantidadeOsConsideradas()).isEqualTo(1L);
    assertThat(resultado.getTempoMedioExecucao())
        .isEqualTo(Duration.between(osValida.getDataAprovacao(), osValida.getDataFechamento()));
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não houver OS para relatório de desempenho")
  void shouldReturnEmptyListWhenNoOrdersForPerformanceReport() {
    JpaOrdemServicoRepository osRepository = mock(JpaOrdemServicoRepository.class);
    JpaMecanicoRepository mecanicoRepository = mock(JpaMecanicoRepository.class);
    RelatorioRepositoryImpl repository =
        new RelatorioRepositoryImpl(osRepository, mecanicoRepository);
    LocalDate inicio = LocalDate.now();
    LocalDate fim = LocalDate.now();

    when(osRepository.findByStatusAndDataFechamentoBetweenAndMecanicoExecucaoIdIsNotNull(
            eq(StatusOS.FINALIZADA), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(Collections.emptyList());

    List<RelatorioDesempenhoMecanico> resultado = repository.gerarRelatorioDesempenho(inicio, fim);

    assertThat(resultado).isEmpty();
  }

  @Test
  @DisplayName("Deve gerar relatório de desempenho corretamente")
  void shouldGeneratePerformanceReportCorrectly() {
    JpaOrdemServicoRepository osRepository = mock(JpaOrdemServicoRepository.class);
    JpaMecanicoRepository mecanicoRepository = mock(JpaMecanicoRepository.class);
    RelatorioRepositoryImpl repository =
        new RelatorioRepositoryImpl(osRepository, mecanicoRepository);
    LocalDate inicio = LocalDate.now().minusDays(1);
    LocalDate fim = LocalDate.now();
    LocalDateTime start = inicio.atStartOfDay();
    LocalDateTime end = fim.atTime(LocalTime.MAX);

    UUID mecanicoId1 = UUID.randomUUID();
    UUID mecanicoId2 = UUID.randomUUID();

    // Mecanico 1: 2 OSs, 1 with null dates (should be ignored for duration)
    OrdemServicoEntity os1 = new OrdemServicoEntity();
    os1.setMecanicoExecucaoId(mecanicoId1);
    os1.setValorTotal(new BigDecimal("100.00"));
    os1.setDataEntrada(start);
    os1.setDataFechamento(start.plusHours(2)); // 2 hours duration

    OrdemServicoEntity os2 = new OrdemServicoEntity();
    os2.setMecanicoExecucaoId(mecanicoId1);
    os2.setValorTotal(new BigDecimal("200.00"));
    os2.setDataEntrada(null); // Should be ignored for duration
    os2.setDataFechamento(null);

    // Mecanico 2: 1 OS
    OrdemServicoEntity os3 = new OrdemServicoEntity();
    os3.setMecanicoExecucaoId(mecanicoId2);
    os3.setValorTotal(new BigDecimal("300.00"));
    os3.setDataEntrada(start);
    os3.setDataFechamento(start.plusHours(5)); // 5 hours duration

    when(osRepository.findByStatusAndDataFechamentoBetweenAndMecanicoExecucaoIdIsNotNull(
            eq(StatusOS.FINALIZADA), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(List.of(os1, os2, os3));

    MecanicoEntity mec1 = new MecanicoEntity();
    mec1.setId(mecanicoId1);
    mec1.setNome("Mecanico Um");

    MecanicoEntity mec2 = new MecanicoEntity();
    mec2.setId(mecanicoId2);
    mec2.setNome("Mecanico Dois");

    when(mecanicoRepository.findAllById(any())).thenReturn(List.of(mec1, mec2));

    List<RelatorioDesempenhoMecanico> resultado = repository.gerarRelatorioDesempenho(inicio, fim);

    assertThat(resultado).hasSize(2);

    // Verify Mecanico 1
    RelatorioDesempenhoMecanico rel1 =
        resultado.stream()
            .filter(r -> r.getMecanicoId().equals(mecanicoId1))
            .findFirst()
            .orElseThrow();
    assertThat(rel1.getNomeMecanico()).isEqualTo("Mecanico Um");
    assertThat(rel1.getQuantidadeOsConcluidas()).isEqualTo(2);
    assertThat(rel1.getReceitaTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
    assertThat(rel1.getTempoMedioConclusao()).isEqualTo(Duration.ofHours(2));

    // Verify Mecanico 2
    RelatorioDesempenhoMecanico rel2 =
        resultado.stream()
            .filter(r -> r.getMecanicoId().equals(mecanicoId2))
            .findFirst()
            .orElseThrow();
    assertThat(rel2.getNomeMecanico()).isEqualTo("Mecanico Dois");
    assertThat(rel2.getQuantidadeOsConcluidas()).isEqualTo(1);
    assertThat(rel2.getReceitaTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
    assertThat(rel2.getTempoMedioConclusao()).isEqualTo(Duration.ofHours(5));
  }

  @Test
  @DisplayName(
      "Deve usar nome desconhecido e tempo zero quando mecânico não é encontrado e sem durações"
          + " válidas")
  void shouldUseUnknownNameAndZeroDurationWhenMechanicNotFoundAndNoValidDurations() {
    JpaOrdemServicoRepository osRepository = mock(JpaOrdemServicoRepository.class);
    JpaMecanicoRepository mecanicoRepository = mock(JpaMecanicoRepository.class);
    RelatorioRepositoryImpl repository =
        new RelatorioRepositoryImpl(osRepository, mecanicoRepository);
    LocalDate inicio = LocalDate.now().minusDays(1);
    LocalDate fim = LocalDate.now();

    UUID mecanicoId = UUID.randomUUID();

    OrdemServicoEntity os = new OrdemServicoEntity();
    os.setMecanicoExecucaoId(mecanicoId);
    os.setValorTotal(new BigDecimal("150.00"));
    os.setDataEntrada(null);
    os.setDataFechamento(null);

    when(osRepository.findByStatusAndDataFechamentoBetweenAndMecanicoExecucaoIdIsNotNull(
            eq(StatusOS.FINALIZADA), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(List.of(os));

    when(mecanicoRepository.findAllById(any())).thenReturn(Collections.emptyList());

    List<RelatorioDesempenhoMecanico> resultado = repository.gerarRelatorioDesempenho(inicio, fim);

    assertThat(resultado).hasSize(1);
    RelatorioDesempenhoMecanico rel = resultado.getFirst();
    assertThat(rel.getMecanicoId()).isEqualTo(mecanicoId);
    assertThat(rel.getNomeMecanico()).isEqualTo("Desconhecido");
    assertThat(rel.getQuantidadeOsConcluidas()).isEqualTo(1);
    assertThat(rel.getReceitaTotal()).isEqualByComparingTo(new BigDecimal("150.00"));
    assertThat(rel.getTempoMedioConclusao()).isEqualTo(Duration.ZERO);
  }
}
