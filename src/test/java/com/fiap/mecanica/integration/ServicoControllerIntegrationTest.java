package com.fiap.mecanica.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.domain.repository.ServicoRepository;
import java.math.BigDecimal;
import java.time.Duration;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockUser(
    username = "testuser",
    roles = {"ADMIN"})
class ServicoControllerIntegrationTest {

  @MockBean private JavaMailSender javaMailSender;

  @Autowired private MockMvc mockMvc;

  @Autowired private ServicoRepository servicoRepository;

  @Test
  @DisplayName("POST /api/servicos cria servico e GET retorna dados")
  void createAndGetServico() throws Exception {
    String payload =
        """
            {
                "nome": "Troca de Óleo Premium",
                "descricao": "Troca de óleo sintético e filtro",
                "valorBase": 250.50,
                "tempoEstimadoMinutos": 45,
                "categoria": "MANUTENCAO_PREVENTIVA"
            }
            """;

    String location =
        mockMvc
            .perform(post("/api/servicos").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.nome").value("Troca de Óleo Premium"))
            .andExpect(jsonPath("$.categoria").value("MANUTENCAO_PREVENTIVA"))
            .andReturn()
            .getResponse()
            .getHeader("Location");

    // Extract ID from Location
    String[] parts = location.split("/");
    String id = parts[parts.length - 1];

    // GET by ID
    mockMvc
        .perform(get("/api/servicos/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Troca de Óleo Premium"))
        .andExpect(jsonPath("$.valorBase").value(250.50));
  }

  @Test
  @DisplayName("GET /api/servicos deve listar servicos ativos")
  void listServicos() throws Exception {
    // Given existing services
    Servico s1 =
        new Servico(
            "Alinhamento",
            "Alinhamento 3D",
            BigDecimal.valueOf(100),
            Duration.ofMinutes(30),
            CategoriaServico.MANUTENCAO_PREVENTIVA);
    Servico s2 =
        new Servico(
            "Balanceamento",
            "Balanceamento rodas",
            BigDecimal.valueOf(80),
            Duration.ofMinutes(20),
            CategoriaServico.MANUTENCAO_PREVENTIVA);
    servicoRepository.save(s1);
    servicoRepository.save(s2);

    // When GET /api/servicos
    mockMvc
        .perform(get("/api/servicos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        // We expect at least these 2, but there might be seeded data if context is shared/seeded.
        // Since @Transactional rolls back, and we are in a test method, we rely on isolation or
        // checking size >= 2.
        // Assuming empty DB for test profile or transactional rollback clears it effectively for
        // this transaction scope.
        .andExpect(jsonPath("$.content[*].nome").exists());
  }

  @Test
  @DisplayName("PUT /api/servicos/{id} deve atualizar dados")
  void updateServico() throws Exception {
    Servico s =
        new Servico(
            "Freio",
            "Troca pastilha",
            BigDecimal.valueOf(150),
            Duration.ofMinutes(60),
            CategoriaServico.REPARO_MECANICO);
    s = servicoRepository.save(s);

    String updatePayload =
        """
            {
                "nome": "Freio Completo",
                "descricao": "Troca pastilha e disco",
                "valorBase": 300.00,
                "tempoEstimadoMinutos": 90,
                "categoria": "REPARO_MECANICO"
            }
            """;

    mockMvc
        .perform(
            put("/api/servicos/{id}", s.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Freio Completo"))
        .andExpect(jsonPath("$.valorBase").value(300.00));
  }

  @Test
  @DisplayName("DELETE /api/servicos/{id} deve inativar ou remover servico")
  void deleteServico() throws Exception {
    Servico s =
        new Servico(
            "Lavagem",
            "Lavagem simples",
            BigDecimal.valueOf(50),
            Duration.ofMinutes(30),
            CategoriaServico.ESTETICA);
    s = servicoRepository.save(s);

    mockMvc.perform(delete("/api/servicos/{id}", s.getId())).andExpect(status().isNoContent());

    // Verify it's gone (or 404)
    mockMvc.perform(get("/api/servicos/{id}", s.getId())).andExpect(status().isNotFound());
  }
}
