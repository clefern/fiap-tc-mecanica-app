package com.fiap.mecanica.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
class ClienteControllerIntegrationTest {

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired MockMvc mockMvc;

  @Test
  @DisplayName("POST /api/clientes cria cliente e GET por documento retorna dados")
  void createAndGetByDocumento() throws Exception {
    String payload =
        "{"
            + "\"nome\":\"João da Silva\","
            + "\"documento\":\"39053344705\","
            + "\"tipoPessoa\":\"FISICA\","
            + "\"email\":\"joao@example.com\","
            + "\"telefone\":\"11987654321\","
            + "\"endereco\":\"Rua A, 123\""
            + "}";

    String location =
        mockMvc
            .perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.nome").value("João da Silva"))
            .andExpect(jsonPath("$.documento").value("39053344705"))
            .andExpect(jsonPath("$.tipoPessoa").value("FISICA"))
            .andReturn()
            .getResponse()
            .getHeader("Location");

    // GET por Documento
    mockMvc
        .perform(get("/api/clientes/documento/{documento}", "39053344705"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("João da Silva"))
        .andExpect(jsonPath("$.email").value("joao@example.com"))
        .andExpect(jsonPath("$.telefone").value("11987654321"))
        .andExpect(jsonPath("$.endereco").value("Rua A, 123"));

    // DELETE pelo Location
    String[] parts = location.split("/");
    String id = parts[parts.length - 1];
    mockMvc.perform(delete("/api/clientes/{id}", id)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/clientes/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST deve retornar 409 ao cadastrar Documento duplicado")
  void duplicateDocumentoReturnsConflict() throws Exception {
    String payload1 =
        "{"
            + "\"nome\":\"Cliente 1\","
            + "\"documento\":\"52998224725\","
            + "\"tipoPessoa\":\"FISICA\","
            + "\"email\":\"c1@example.com\","
            + "\"telefone\":\"11987654321\","
            + "\"endereco\":\"Rua B, 456\""
            + "}";
    String payload2 =
        "{"
            + "\"nome\":\"Cliente 2\","
            + "\"documento\":\"52998224725\","
            + "\"tipoPessoa\":\"FISICA\","
            + "\"email\":\"c2@example.com\","
            + "\"telefone\":\"11987654321\","
            + "\"endereco\":\"Rua C, 789\""
            + "}";

    mockMvc
        .perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(payload1))
        .andExpect(status().isCreated());

    mockMvc
        .perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(payload2))
        .andExpect(status().isConflict());
  }
}
