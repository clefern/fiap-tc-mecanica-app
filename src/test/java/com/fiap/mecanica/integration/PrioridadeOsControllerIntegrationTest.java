package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.infra.jpa.JpaClienteRepository;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import com.fiap.mecanica.infra.jpa.JpaVeiculoRepository;
import com.fiap.mecanica.infra.seeding.factory.ClienteFactory;
import com.fiap.mecanica.infra.seeding.factory.VeiculoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrioridadeOsControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  // Domain Repositories for Setup
  @Autowired private OrdemServicoRepository osRepository;
  @Autowired private ClienteRepository clienteRepository;
  @Autowired private VeiculoRepository veiculoRepository;

  // JPA Repositories for Cleanup
  @Autowired private JpaOrdemServicoRepository jpaOsRepository;
  @Autowired private JpaVeiculoRepository jpaVeiculoRepository;
  @Autowired private JpaClienteRepository jpaClienteRepository;

  @Autowired private ClienteFactory clienteFactory;
  @Autowired private VeiculoFactory veiculoFactory;

  @MockBean private JavaMailSender javaMailSender;

  private Cliente cliente;
  private Veiculo veiculo;

  @BeforeEach
  void setUp() {
    jpaOsRepository.deleteAll();
    jpaVeiculoRepository.deleteAll();
    jpaClienteRepository.deleteAll();

    cliente = clienteRepository.save(clienteFactory.create());
    veiculo = veiculoFactory.create();
    veiculo = veiculoRepository.save(cliente.getId(), veiculo);
  }

  @Test
  @DisplayName("Deve listar fila de orçamento ordenada por prioridade (URGENTE -> BAIXA)")
  @WithMockUser(roles = "ATENDENTE")
  void deveListarFilaOrcamento() throws Exception {
    // Arrange
    createOS(StatusOS.RECEBIDA, Prioridade.BAIXA);
    createOS(StatusOS.RECEBIDA, Prioridade.URGENTE);
    createOS(StatusOS.RECEBIDA, Prioridade.NORMAL);

    // Act & Assert
    mockMvc
        .perform(get("/api/prioridade/fila-orcamento").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(3)))
        .andExpect(jsonPath("$.content[0].prioridade").value("URGENTE"))
        .andExpect(jsonPath("$.content[1].prioridade").value("NORMAL"))
        .andExpect(jsonPath("$.content[2].prioridade").value("BAIXA"));
  }

  @Test
  @DisplayName("Deve listar fila de execução ordenada por prioridade (ALTA -> NORMAL)")
  @WithMockUser(roles = "MECANICO")
  void deveListarFilaExecucao() throws Exception {
    // Arrange
    // Orçamento deve estar APROVADA para estar na fila de execução
    createOS(StatusOS.APROVADA, Prioridade.NORMAL);
    createOS(StatusOS.APROVADA, Prioridade.ALTA);

    // Act & Assert
    mockMvc
        .perform(get("/api/prioridade/fila-execucao").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].prioridade").value("ALTA"))
        .andExpect(jsonPath("$.content[1].prioridade").value("NORMAL"));
  }

  @Test
  @DisplayName("Deve obter a próxima OS para orçamento (maior prioridade)")
  @WithMockUser(roles = "MECANICO")
  void deveObterProximaParaOrcamento() throws Exception {
    // Arrange
    createOS(StatusOS.RECEBIDA, Prioridade.BAIXA);
    OrdemServico urgente = createOS(StatusOS.RECEBIDA, Prioridade.URGENTE);

    // Act & Assert
    mockMvc
        .perform(get("/api/prioridade/proxima").param("tipo", "ORCAMENTO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(urgente.getId().toString()))
        .andExpect(jsonPath("$.prioridade").value("URGENTE"));
  }

  @Test
  @DisplayName("Deve obter a próxima OS para execução (maior prioridade)")
  @WithMockUser(roles = "MECANICO")
  void deveObterProximaParaExecucao() throws Exception {
    // Arrange
    createOS(StatusOS.APROVADA, Prioridade.NORMAL);
    OrdemServico alta = createOS(StatusOS.APROVADA, Prioridade.ALTA);

    // Act & Assert
    mockMvc
        .perform(get("/api/prioridade/proxima").param("tipo", "EXECUCAO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(alta.getId().toString()))
        .andExpect(jsonPath("$.prioridade").value("ALTA"));
  }

  @Test
  @DisplayName("Deve atualizar a prioridade de uma OS (Gerente)")
  @WithMockUser(roles = "GERENTE")
  void deveAtualizarPrioridade() throws Exception {
    // Arrange
    OrdemServico os = createOS(StatusOS.RECEBIDA, Prioridade.BAIXA);

    // Act & Assert
    mockMvc
        .perform(
            put("/api/prioridade/{id}", os.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Prioridade.URGENTE))) // Envia enum direto ou objeto? Controller espera
        // @RequestBody Prioridade
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.prioridade").value("URGENTE"));

    // Verify persistence
    OrdemServico updated = osRepository.findById(os.getId()).orElseThrow();
    assertThat(updated.getPrioridade()).isEqualTo(Prioridade.URGENTE);
  }

  @Test
  @DisplayName("Não deve permitir atualizar prioridade sem role de GERENTE")
  @WithMockUser(roles = "ATENDENTE")
  void naoDeveAtualizarPrioridadeSemPermissao() throws Exception {
    // Arrange
    OrdemServico os = createOS(StatusOS.RECEBIDA, Prioridade.BAIXA);

    // Act & Assert
    mockMvc
        .perform(
            put("/api/prioridade/{id}", os.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"URGENTE\""))
        .andExpect(status().isForbidden());
  }

  private OrdemServico createOS(StatusOS status, Prioridade prioridade) {
    OrdemServico os = OrdemServico.nova(cliente.getId(), veiculo.getId());
    os.setStatus(status);
    os.setPrioridade(prioridade);
    // Needed for ordering by date if priorities match, but UUID randomizes creation order slightly
    // in practice?
    // No, entity listener sets CreatedAt.
    // Sleep to ensure different timestamps if needed, but priority should suffice.
    if (status == StatusOS.APROVADA) {
      os.aprovar(); // Sets dataAprovacao
    }
    return osRepository.save(os);
  }
}
