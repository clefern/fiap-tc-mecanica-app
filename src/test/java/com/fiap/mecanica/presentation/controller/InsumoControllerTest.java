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
import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.domain.enums.StatusEstoque;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.infra.config.security.JwtService;
import com.fiap.mecanica.presentation.dto.InsumoRequest;
import com.fiap.mecanica.presentation.dto.InsumoResponse;
import com.fiap.mecanica.presentation.mapper.InsumoMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InsumoController.class)
@AutoConfigureMockMvc(addFilters = false)
class InsumoControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private InsumoService service;

  @MockitoBean private InsumoMapper mapper;

  @MockitoBean private JwtService jwtService;

  @MockitoBean private UserDetailsService userDetailsService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("Deve criar insumo com sucesso")
  void shouldCreateInsumo() throws Exception {
    InsumoRequest request =
        new InsumoRequest(
            "Oleo", "Oleo motor", new BigDecimal("50.00"), "LITRO", true, 100, 20, 200);
    Insumo domainObj =
        new Insumo(
            null, "Oleo", "Oleo motor", new BigDecimal("50.00"), true, "LITRO", 100, 20, 200);
    Insumo insumo =
        new Insumo(
            UUID.randomUUID(),
            "Oleo",
            "Oleo motor",
            new BigDecimal("50.00"),
            true,
            "LITRO",
            100,
            20,
            200);
    InsumoResponse response =
        new InsumoResponse(
            insumo.getId(),
            "Oleo",
            "Oleo motor",
            new BigDecimal("50.00"),
            "LITRO",
            true,
            100,
            20,
            200,
            StatusEstoque.NORMAL);

    when(mapper.toDomain(any(InsumoRequest.class))).thenReturn(domainObj);
    when(service.create(any(Insumo.class))).thenReturn(insumo);
    when(mapper.toResponse(insumo)).thenReturn(response);

    mockMvc
        .perform(
            post("/api/insumos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nome").value("Oleo"))
        .andExpect(jsonPath("$.unidadeMedida").value("LITRO"))
        .andExpect(jsonPath("$.quantidadeEstoque").value(100));
  }

  @Test
  @DisplayName("Deve listar todos os insumos")
  void shouldListAllInsumos() throws Exception {
    Insumo insumo =
        new Insumo(
            UUID.randomUUID(),
            "Oleo",
            "Desc",
            new BigDecimal("50.00"),
            true,
            "LITRO",
            100,
            20,
            200);
    InsumoResponse response =
        new InsumoResponse(
            insumo.getId(),
            "Oleo",
            "Desc",
            new BigDecimal("50.00"),
            "LITRO",
            true,
            100,
            20,
            200,
            StatusEstoque.NORMAL);
    Page<Insumo> page = new PageImpl<>(List.of(insumo));

    when(service.getAll(any(Pageable.class))).thenReturn(page);
    when(mapper.toResponse(insumo)).thenReturn(response);

    mockMvc
        .perform(get("/api/insumos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].nome").value("Oleo"))
        .andExpect(jsonPath("$.content[0].quantidadeEstoque").value(100));
  }

  @Test
  @DisplayName("Deve listar insumos ativos")
  void shouldListActiveInsumos() throws Exception {
    Insumo insumo =
        new Insumo(
            UUID.randomUUID(),
            "Oleo",
            "Desc",
            new BigDecimal("50.00"),
            true,
            "LITRO",
            100,
            20,
            200);
    InsumoResponse response =
        new InsumoResponse(
            insumo.getId(),
            "Oleo",
            "Desc",
            new BigDecimal("50.00"),
            "LITRO",
            true,
            100,
            20,
            200,
            StatusEstoque.NORMAL);
    Page<Insumo> page = new PageImpl<>(List.of(insumo));

    when(service.getAllAtivos(any(Pageable.class))).thenReturn(page);
    when(mapper.toResponse(insumo)).thenReturn(response);

    mockMvc
        .perform(get("/api/insumos/ativos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].ativo").value(true))
        .andExpect(jsonPath("$.content[0].quantidadeEstoque").value(100));
  }

  @Test
  @DisplayName("Deve buscar insumos por termo")
  void shouldSearchInsumosByTerm() throws Exception {
    Insumo insumo =
        new Insumo(
            UUID.randomUUID(),
            "Óleo Sintético",
            "Desc",
            new BigDecimal("50.00"),
            true,
            "LITRO",
            100,
            20,
            200);
    InsumoResponse response =
        new InsumoResponse(
            insumo.getId(),
            insumo.getNome(),
            insumo.getDescricao(),
            insumo.getPrecoBase(),
            insumo.getUnidadeMedida(),
            insumo.isAtivo(),
            insumo.getQuantidadeEstoque(),
            insumo.getEstoqueMinimo(),
            insumo.getEstoqueMaximo(),
            StatusEstoque.NORMAL);
    Page<Insumo> page = new PageImpl<>(List.of(insumo));

    when(service.search(eq("oleo"), any(Pageable.class))).thenReturn(page);
    when(mapper.toResponse(insumo)).thenReturn(response);

    mockMvc
        .perform(get("/api/insumos/search").param("termo", "oleo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].nome").value("Óleo Sintético"));
  }

  @Test
  @DisplayName("Deve buscar insumo por ID")
  void shouldGetInsumoById() throws Exception {
    UUID id = UUID.randomUUID();
    Insumo insumo =
        new Insumo(id, "Oleo", "Desc", new BigDecimal("50.00"), true, "LITRO", 100, 20, 200);
    InsumoResponse response =
        new InsumoResponse(
            id,
            "Oleo",
            "Desc",
            new BigDecimal("50.00"),
            "LITRO",
            true,
            100,
            20,
            200,
            StatusEstoque.NORMAL);

    when(service.getById(id)).thenReturn(Optional.of(insumo));
    when(mapper.toResponse(insumo)).thenReturn(response);

    mockMvc
        .perform(get("/api/insumos/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.quantidadeEstoque").value(100));
  }

  @Test
  @DisplayName("Deve retornar 404 quando insumo não encontrado")
  void shouldReturn404WhenInsumoNotFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/insumos/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve atualizar insumo")
  void shouldUpdateInsumo() throws Exception {
    UUID id = UUID.randomUUID();
    InsumoRequest request =
        new InsumoRequest(
            "Oleo Up", "Desc Up", new BigDecimal("60.00"), "LITRO", true, 100, 20, 200);
    Insumo domainObj =
        new Insumo(
            null, "Oleo Up", "Desc Up", new BigDecimal("60.00"), true, "LITRO", 100, 20, 200);
    Insumo updated =
        new Insumo(id, "Oleo Up", "Desc Up", new BigDecimal("60.00"), true, "LITRO", 100, 20, 200);
    InsumoResponse response =
        new InsumoResponse(
            id,
            "Oleo Up",
            "Desc Up",
            new BigDecimal("60.00"),
            "LITRO",
            true,
            100,
            20,
            200,
            StatusEstoque.NORMAL);

    when(mapper.toDomain(any(InsumoRequest.class))).thenReturn(domainObj);
    when(service.update(eq(id), any(Insumo.class))).thenReturn(updated);
    when(mapper.toResponse(updated)).thenReturn(response);

    mockMvc
        .perform(
            put("/api/insumos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Oleo Up"))
        .andExpect(jsonPath("$.quantidadeEstoque").value(100));
  }

  @Test
  @DisplayName("Deve deletar insumo")
  void shouldDeleteInsumo() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(service).delete(id);

    mockMvc.perform(delete("/api/insumos/{id}", id)).andExpect(status().isNoContent());
  }
}
