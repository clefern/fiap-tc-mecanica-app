package com.fiap.mecanica.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class VeiculoControllerIntegrationTest {

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired MockMvc mockMvc;

  @Test
  @DisplayName("Fluxo: cria Cliente, cria Veiculo, consulta por placa e remove por placa")
  void createGetAndDeleteByPlaca() throws Exception {
    // Cria cliente base
    String clientePayload =
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
            .perform(
                post("/api/clientes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(clientePayload))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andReturn()
            .getResponse()
            .getHeader("Location");

    String[] parts = location.split("/");
    String clienteId = parts[parts.length - 1];

    // Cria veículo para o cliente
    String veiculoPayload =
        "{"
            + "\"placa\":\"XYZ1A23\","
            + "\"marca\":\"Ford\","
            + "\"modelo\":\"Fiesta\","
            + "\"ano\":2018"
            + "}";

    mockMvc
        .perform(
            post("/api/clientes/{clienteId}/veiculos", clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(veiculoPayload))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/veiculos/XYZ1A23"))
        .andExpect(jsonPath("$.placa").value("XYZ1A23"))
        .andExpect(jsonPath("$.marca").value("Ford"))
        .andExpect(jsonPath("$.modelo").value("Fiesta"))
        .andExpect(jsonPath("$.ano").value(2018));

    // Consulta por placa
    mockMvc
        .perform(get("/api/veiculos/{placa}", "XYZ1A23"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.marca").value("Ford"))
        .andExpect(jsonPath("$.modelo").value("Fiesta"))
        .andExpect(jsonPath("$.ano").value(2018));

    // Remove por placa
    mockMvc.perform(delete("/api/veiculos/{placa}", "XYZ1A23")).andExpect(status().isNoContent());

    // Verifica 404 após remoção
    mockMvc.perform(get("/api/veiculos/{placa}", "XYZ1A23")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Erro 409 ao tentar cadastrar mesma placa")
  void duplicatePlacaReturnsConflict() throws Exception {
    // Cria cliente
    String clientePayload =
        "{"
            + "\"nome\":\"Maria Souza\","
            + "\"documento\":\"52998224725\","
            + "\"tipoPessoa\":\"FISICA\","
            + "\"email\":\"maria.souza@example.com\","
            + "\"telefone\":\"11999998888\","
            + "\"endereco\":\"Rua B, 456\""
            + "}";

    String location =
        mockMvc
            .perform(
                post("/api/clientes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(clientePayload))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getHeader("Location");
    String[] parts = location.split("/");
    String clienteId = parts[parts.length - 1];

    String v1 =
        "{"
            + "\"placa\":\"XYZ1B23\","
            + "\"marca\":\"Ford\","
            + "\"modelo\":\"Fiesta\","
            + "\"ano\":2018"
            + "}";
    String v2 =
        "{"
            + "\"placa\":\"XYZ1B23\","
            + "\"marca\":\"Ford\","
            + "\"modelo\":\"Ka\","
            + "\"ano\":2019"
            + "}";

    mockMvc
        .perform(
            post("/api/clientes/{clienteId}/veiculos", clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(v1))
        .andDo(print())
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/clientes/{clienteId}/veiculos", clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(v2))
        .andDo(print())
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("Lista veículos de um cliente")
  void listVeiculosByCliente() throws Exception {
    // Cria cliente
    String clientePayload =
        "{"
            + "\"nome\":\"Pedro Paulo\","
            + "\"documento\":\"12345678909\","
            + "\"tipoPessoa\":\"FISICA\","
            + "\"email\":\"pedro.paulo@example.com\","
            + "\"telefone\":\"11977776666\","
            + "\"endereco\":\"Rua C, 789\""
            + "}";

    String location =
        mockMvc
            .perform(
                post("/api/clientes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(clientePayload))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getHeader("Location");
    String[] parts = location.split("/");
    String clienteId = parts[parts.length - 1];

    String v1 =
        "{"
            + "\"placa\":\"XYZ1A23\","
            + "\"marca\":\"VW\","
            + "\"modelo\":\"Gol\","
            + "\"ano\":2015"
            + "}";
    String v2 =
        "{"
            + "\"placa\":\"XYZ1B23\","
            + "\"marca\":\"VW\","
            + "\"modelo\":\"Polo\","
            + "\"ano\":2020"
            + "}";

    mockMvc
        .perform(
            post("/api/clientes/{clienteId}/veiculos", clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(v1))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/clientes/{clienteId}/veiculos", clienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(v2))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/clientes/{clienteId}/veiculos", clienteId))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].placa").exists())
        .andExpect(jsonPath("$[1].placa").exists())
        .andExpect(jsonPath("$[0].marca").value("VW"));
  }
}
