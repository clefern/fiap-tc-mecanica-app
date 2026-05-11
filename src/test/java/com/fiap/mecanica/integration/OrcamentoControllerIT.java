package com.fiap.mecanica.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.repository.OrcamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrcamentoControllerIT {

  @MockBean private JavaMailSender javaMailSender;

  @Autowired private MockMvc mockMvc;

  @Autowired private OrcamentoRepository orcamentoRepository;

  private Orcamento orcamentoSalvo;

  @BeforeEach
  void setUp() {
    orcamentoSalvo =
        Orcamento.builder()
            .codigo("IT-ORC-" + UUID.randomUUID().toString().substring(0, 8))
            .ordemServicoId(UUID.randomUUID())
            .dataEmissao(LocalDateTime.now())
            .dataValidade(LocalDateTime.now().plusDays(10))
            .valorTotalMateriais(new BigDecimal("100.00"))
            .valorTotalMaoDeObra(new BigDecimal("50.00"))
            .valorImpostos(new BigDecimal("5.00"))
            .valorTotal(new BigDecimal("155.00"))
            .status(StatusOrcamento.GERADO)
            .build();

    orcamentoSalvo = orcamentoRepository.save(orcamentoSalvo);
  }

  @Test
  @DisplayName("Deve listar orçamentos (Integration)")
  @WithMockUser(roles = "ATENDENTE")
  void deveListarOrcamentos() throws Exception {
    mockMvc
        .perform(get("/api/orcamentos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value(orcamentoSalvo.getId().toString()));
  }

  @Test
  @DisplayName("Deve buscar orçamento por ID (Integration)")
  @WithMockUser(roles = "ATENDENTE")
  void deveBuscarPorId() throws Exception {
    mockMvc
        .perform(get("/api/orcamentos/{id}", orcamentoSalvo.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orcamentoSalvo.getId().toString()));
  }

  @Test
  @DisplayName("Deve buscar orçamento por OS ID (Integration)")
  @WithMockUser(roles = "MECANICO")
  void deveBuscarPorOsId() throws Exception {
    mockMvc
        .perform(get("/api/orcamentos/os/{id}", orcamentoSalvo.getOrdemServicoId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orcamentoSalvo.getId().toString()));
  }

  @Test
  @DisplayName("Deve deletar orçamento (Integration - Admin)")
  @WithMockUser(roles = "ADMIN")
  void deveDeletarOrcamento() throws Exception {
    mockMvc
        .perform(delete("/api/orcamentos/{id}", orcamentoSalvo.getId()))
        .andExpect(status().isNoContent());

    // Verifica se foi deletado
    mockMvc
        .perform(get("/api/orcamentos/{id}", orcamentoSalvo.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Não deve deletar orçamento sem permissão (Integration - User)")
  @WithMockUser(roles = "ATENDENTE")
  void naoDeveDeletarSemPermissao() throws Exception {
    mockMvc
        .perform(delete("/api/orcamentos/{id}", orcamentoSalvo.getId()))
        .andExpect(status().isForbidden());
  }
}
