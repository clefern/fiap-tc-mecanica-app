package com.fiap.mecanica.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.infra.config.security.JwtService;
import com.fiap.mecanica.presentation.dto.PecaRequest;
import com.fiap.mecanica.presentation.mapper.PecaMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PecaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PecaMapper.class)
class PecaControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PecaService service;

  @MockBean private JwtService jwtService;

  @MockBean private UserDetailsService userDetailsService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("Deve criar peça com sucesso")
  void shouldCreatePeca() throws Exception {
    PecaRequest request =
        new PecaRequest(
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            "Fab 1",
            "Cod 1",
            "Mod 1",
            true,
            50,
            10,
            100);
    Peca peca =
        new Peca(
            UUID.randomUUID(),
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            50,
            10,
            100);

    when(service.create(any(Peca.class))).thenReturn(peca);

    mockMvc
        .perform(
            post("/api/pecas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nome").value("Peca 1"))
        .andExpect(jsonPath("$.fabricante").value("Fab 1"))
        .andExpect(jsonPath("$.quantidadeEstoque").value(50));
  }

  @Test
  @DisplayName("Deve listar todas as peças")
  void shouldListAllPecas() throws Exception {
    Peca peca =
        new Peca(
            UUID.randomUUID(),
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            50,
            10,
            100);
    Page<Peca> page = new PageImpl<>(List.of(peca));
    when(service.getAll(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/pecas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].nome").value("Peca 1"))
        .andExpect(jsonPath("$.content[0].quantidadeEstoque").value(50));
  }

  @Test
  @DisplayName("Deve listar peças ativas")
  void shouldListActivePecas() throws Exception {
    Peca peca =
        new Peca(
            UUID.randomUUID(),
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            50,
            10,
            100);
    Page<Peca> page = new PageImpl<>(List.of(peca));
    when(service.getAllAtivos(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/pecas/ativas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].ativo").value(true))
        .andExpect(jsonPath("$.content[0].quantidadeEstoque").value(50));
  }

  @Test
  @DisplayName("Deve buscar peças por termo")
  void shouldSearchPecasByTerm() throws Exception {
    Peca peca =
        new Peca(
            UUID.randomUUID(),
            "Filtro de Óleo",
            "Desc filtro",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            50,
            10,
            100);
    Page<Peca> page = new PageImpl<>(List.of(peca));
    when(service.search(eq("filtro"), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/pecas/search").param("termo", "filtro"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].nome").value("Filtro de Óleo"));
  }

  @Test
  @DisplayName("Deve buscar peça por ID")
  void shouldGetPecaById() throws Exception {
    UUID id = UUID.randomUUID();
    Peca peca =
        new Peca(
            id,
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            50,
            10,
            100);
    when(service.getById(id)).thenReturn(Optional.of(peca));

    mockMvc
        .perform(get("/api/pecas/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Peca 1"))
        .andExpect(jsonPath("$.quantidadeEstoque").value(50));
  }

  @Test
  @DisplayName("Deve retornar 404 quando peça não encontrada")
  void shouldReturn404WhenPecaNotFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/pecas/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve atualizar peça")
  void shouldUpdatePeca() throws Exception {
    UUID id = UUID.randomUUID();
    PecaRequest request =
        new PecaRequest(
            "Peca Up",
            "Desc Up",
            new BigDecimal("150.00"),
            "Fab 2",
            "Cod 2",
            "Mod 2",
            true,
            50,
            10,
            100);
    Peca updated =
        new Peca(
            id,
            "Peca Up",
            "Desc Up",
            new BigDecimal("150.00"),
            true,
            "Fab 2",
            "Cod 2",
            "Mod 2",
            50,
            10,
            100);

    when(service.update(eq(id), any(Peca.class))).thenReturn(updated);

    mockMvc
        .perform(
            put("/api/pecas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Peca Up"))
        .andExpect(jsonPath("$.quantidadeEstoque").value(50));
  }

  @Test
  @DisplayName("Deve deletar peça")
  void shouldDeletePeca() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(service).delete(id);

    mockMvc.perform(delete("/api/pecas/{id}", id)).andExpect(status().isNoContent());
  }
}
