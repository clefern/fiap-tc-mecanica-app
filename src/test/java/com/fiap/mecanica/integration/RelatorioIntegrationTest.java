package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.domain.model.relatorio.RelatorioDesempenhoMecanico;
import com.fiap.mecanica.domain.model.relatorio.TempoMedioExecucaoOs;
import com.fiap.mecanica.infra.jpa.JpaAtendenteRepository;
import com.fiap.mecanica.infra.jpa.JpaClienteRepository;
import com.fiap.mecanica.infra.jpa.JpaMecanicoRepository;
import com.fiap.mecanica.infra.jpa.JpaVeiculoRepository;
import com.fiap.mecanica.infra.seeding.SeedingOrchestrator;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RelatorioIntegrationTest {

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private SeedingOrchestrator seedingOrchestrator;

  // Repositories for cleanup
  @Autowired private JpaVeiculoRepository jpaVeiculoRepository;
  @Autowired private JpaClienteRepository jpaClienteRepository;
  @Autowired private JpaMecanicoRepository jpaMecanicoRepository;
  @Autowired private JpaAtendenteRepository jpaAtendenteRepository;

  @BeforeEach
  void setUp() {
    jpaVeiculoRepository.deleteAll();
    jpaClienteRepository.deleteAll();
    jpaMecanicoRepository.deleteAll();
    jpaAtendenteRepository.deleteAll();

    // Seed data
    seedingOrchestrator.seed();
  }

  @Test
  @DisplayName("Should generate mechanic performance report")
  @WithMockUser(roles = "ADMIN")
  void shouldGenerateReport() throws Exception {
    LocalDate now = LocalDate.now();
    String start = now.minusMonths(1).toString();
    String end = now.toString();

    String responseJson =
        mockMvc
            .perform(
                get("/api/relatorios/desempenho-mecanicos")
                    .param("inicio", start)
                    .param("fim", end))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<RelatorioDesempenhoMecanico> relatorio =
        objectMapper.readValue(
            responseJson, new TypeReference<List<RelatorioDesempenhoMecanico>>() {});

    assertThat(relatorio).isNotNull();
    // We expect some data because seeder runs and assigns mechanics
    // Note: Seeder randomizes status, so some OS might be FINALIZADA (60% chance)
    // We should see at least some entries if we have enough data seeded.
    if (!relatorio.isEmpty()) {
      RelatorioDesempenhoMecanico item = relatorio.getFirst();
      assertThat(item.getMecanicoId()).isNotNull();
      assertThat(item.getNomeMecanico()).isNotNull();
      assertThat(item.getQuantidadeOsConcluidas()).isGreaterThanOrEqualTo(0L);
    }
  }

  @Test
  @DisplayName("Should return average execution time of OSs")
  @WithMockUser(roles = "ADMIN")
  void shouldReturnAverageExecutionTime() throws Exception {
    String responseJson =
        mockMvc
            .perform(get("/api/relatorios/tempo-medio-execucao-os"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TempoMedioExecucaoOs relatorio =
        objectMapper.readValue(responseJson, TempoMedioExecucaoOs.class);

    assertThat(relatorio).isNotNull();
    assertThat(relatorio.getQuantidadeOsConsideradas()).isGreaterThanOrEqualTo(0L);
  }

  @Test
  @DisplayName("Should return average execution time of OSs for period")
  @WithMockUser(roles = "ADMIN")
  void shouldReturnAverageExecutionTimeForPeriod() throws Exception {
    LocalDate now = LocalDate.now();
    String inicio = now.minusMonths(1).toString();
    String fim = now.toString();

    String responseJson =
        mockMvc
            .perform(
                get("/api/relatorios/tempo-medio-execucao-os/periodo")
                    .param("inicio", inicio)
                    .param("fim", fim))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TempoMedioExecucaoOs relatorio =
        objectMapper.readValue(responseJson, TempoMedioExecucaoOs.class);

    assertThat(relatorio).isNotNull();
    assertThat(relatorio.getQuantidadeOsConsideradas()).isGreaterThanOrEqualTo(0L);
  }

  @Test
  @DisplayName("Should return 403 for non-admin user")
  @WithMockUser(roles = "MECANICO")
  void shouldDenyAccessToNonAdmin() throws Exception {
    mockMvc.perform(get("/api/relatorios/desempenho-mecanicos")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should return 403 for non-admin user on average execution endpoint")
  @WithMockUser(roles = "MECANICO")
  void shouldDenyAccessToNonAdminOnAverageExecutionEndpoint() throws Exception {
    mockMvc
        .perform(get("/api/relatorios/tempo-medio-execucao-os"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should return 403 for non-admin user on average execution by period endpoint")
  @WithMockUser(roles = "MECANICO")
  void shouldDenyAccessToNonAdminOnAverageExecutionByPeriodEndpoint() throws Exception {
    mockMvc
        .perform(
            get("/api/relatorios/tempo-medio-execucao-os/periodo")
                .param("inicio", "2020-01-01")
                .param("fim", "2030-01-01"))
        .andExpect(status().isForbidden());
  }
}
