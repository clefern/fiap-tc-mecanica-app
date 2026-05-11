package com.fiap.mecanica.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.application.service.ServicoService;
import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.infra.config.security.JwtService;
import com.fiap.mecanica.presentation.dto.ServicoRequest;
import com.fiap.mecanica.presentation.mapper.ServicoMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

@WebMvcTest(ServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ServicoMapper.class)
class ServicoControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ServicoService service;

  @MockBean private JwtService jwtService;

  @MockBean private UserDetailsService userDetailsService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void deveCriarServicoComSucesso() throws Exception {
    ServicoRequest request =
        new ServicoRequest(
            "Troca de Pneu",
            "Troca de 4 pneus",
            new BigDecimal("100.00"),
            60L,
            CategoriaServico.MANUTENCAO_PREVENTIVA,
            true);

    Servico servico =
        new Servico(
            UUID.randomUUID(),
            request.getNome(),
            request.getDescricao(),
            request.getValorBase(),
            true,
            Duration.ofMinutes(request.getTempoEstimadoMinutos()),
            request.getCategoria());

    when(service.create(any(Servico.class))).thenReturn(servico);

    mockMvc
        .perform(
            post("/api/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nome").value("Troca de Pneu"))
        .andExpect(jsonPath("$.categoria").value("MANUTENCAO_PREVENTIVA"));
  }

  @Test
  void deveRetornarErroValidacao() throws Exception {
    ServicoRequest request = new ServicoRequest(); // Vazio

    mockMvc
        .perform(
            post("/api/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deveListarTodosServicos() throws Exception {
    Servico s1 =
        new Servico(
            UUID.randomUUID(),
            "S1",
            "D1",
            BigDecimal.TEN,
            true,
            Duration.ofMinutes(30),
            CategoriaServico.OUTROS);
    Page<Servico> page = new PageImpl<>(List.of(s1));
    when(service.getAll(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/servicos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].nome").value("S1"));
  }

  @Test
  void deveListarServicosAtivos() throws Exception {
    Servico s1 =
        new Servico(
            UUID.randomUUID(),
            "S1",
            "D1",
            BigDecimal.TEN,
            true,
            Duration.ofMinutes(30),
            CategoriaServico.OUTROS);
    Page<Servico> page = new PageImpl<>(List.of(s1));
    when(service.getAllAtivos(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/servicos/ativos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].nome").value("S1"));
  }

  @Test
  void deveObterServicoPorId() throws Exception {
    UUID id = UUID.randomUUID();
    Servico s1 =
        new Servico(
            id, "S1", "D1", BigDecimal.TEN, true, Duration.ofMinutes(30), CategoriaServico.OUTROS);
    when(service.getById(id)).thenReturn(Optional.of(s1));

    mockMvc
        .perform(get("/api/servicos/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  void deveRetornar404QuandoServicoNaoEncontrado() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.getById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/servicos/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  void deveAtualizarServico() throws Exception {
    UUID id = UUID.randomUUID();
    ServicoRequest request =
        new ServicoRequest(
            "Novo Nome",
            "Nova Desc",
            BigDecimal.TEN,
            60L,
            CategoriaServico.MANUTENCAO_PREVENTIVA,
            true);
    Servico updated =
        new Servico(
            id,
            "Novo Nome",
            "Nova Desc",
            BigDecimal.TEN,
            true,
            Duration.ofMinutes(60),
            CategoriaServico.MANUTENCAO_PREVENTIVA);

    when(service.update(any(UUID.class), any(Servico.class))).thenReturn(updated);

    mockMvc
        .perform(
            put("/api/servicos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Novo Nome"));
  }

  @Test
  void deveRetornar404AoAtualizarInexistente() throws Exception {
    UUID id = UUID.randomUUID();
    ServicoRequest request =
        new ServicoRequest(
            "Novo Nome",
            "Nova Desc",
            BigDecimal.TEN,
            60L,
            CategoriaServico.MANUTENCAO_PREVENTIVA,
            true);

    when(service.update(any(UUID.class), any(Servico.class)))
        .thenThrow(new ServicoNaoEncontradoException(id));

    mockMvc
        .perform(
            put("/api/servicos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void deveExcluirServico() throws Exception {
    UUID id = UUID.randomUUID();
    Mockito.doNothing().when(service).delete(id);

    mockMvc.perform(delete("/api/servicos/{id}", id)).andExpect(status().isNoContent());
  }
}
