package com.fiap.mecanica.infra.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrcamentoRepository;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MonitoredOperationMetricIT {

  @Autowired private OrcamentoService orcamentoService;

  @Autowired private OrdemServicoRepository ordemServicoRepository;

  @Autowired private OrcamentoRepository orcamentoRepository;

  @Autowired private MeterRegistry meterRegistry;

  private OrdemServico ordemServico;

  @BeforeEach
  void setUp() {
    ordemServico = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    ordemServico = ordemServicoRepository.save(ordemServico);
  }

  @Test
  void shouldRecordMetricsForGerarOrcamento() {
    Orcamento orcamento = orcamentoService.gerarOrcamento(ordemServico);

    assertThat(orcamento).isNotNull();

    double count =
        meterRegistry
            .find("mecanica.service.execution")
            .tag("service", "OrcamentoServiceImpl")
            .tag("operation", "orcamento.gerar")
            .tag("status", "success")
            .timer()
            .count();

    assertThat(count).isGreaterThan(0d);
  }
}
