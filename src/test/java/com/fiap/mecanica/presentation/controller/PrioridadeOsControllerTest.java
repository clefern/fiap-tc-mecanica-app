package com.fiap.mecanica.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.presentation.dto.OrdemServicoResponse;
import com.fiap.mecanica.presentation.mapper.OrdemServicoMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PrioridadeOsControllerTest {

  private MockMvc mockMvc;

  @Mock private PrioridadeService service;
  @Mock private OrdemServicoMapper mapper;

  @InjectMocks private PrioridadeOsController controller;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  @Test
  @DisplayName("Deve listar fila de orçamento paginada")
  void shouldListBudgetQueuePaged() throws Exception {
    OrdemServico os = new OrdemServico();
    os.setId(UUID.randomUUID());
    Page<OrdemServico> page =
        new PageImpl<>(new ArrayList<>(Collections.singletonList(os)), PageRequest.of(0, 10), 1);
    OrdemServicoResponse response = new OrdemServicoResponse();
    response.setId(os.getId());

    when(service.listarFilaOrcamento(any(Pageable.class))).thenReturn(page);
    when(mapper.toResponse(any(OrdemServico.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/prioridade/fila-orcamento"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(os.getId().toString()));
  }

  @Test
  @DisplayName("Deve listar fila de execução paginada")
  void shouldListExecutionQueuePaged() throws Exception {
    OrdemServico os = new OrdemServico();
    os.setId(UUID.randomUUID());
    Page<OrdemServico> page =
        new PageImpl<>(new ArrayList<>(Collections.singletonList(os)), PageRequest.of(0, 10), 1);
    OrdemServicoResponse response = new OrdemServicoResponse();
    response.setId(os.getId());

    when(service.listarFilaExecucao(any(Pageable.class))).thenReturn(page);
    when(mapper.toResponse(any(OrdemServico.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/prioridade/fila-execucao"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(os.getId().toString()));
  }

  @Test
  @DisplayName("Deve obter próxima OS para orçamento")
  void shouldGetNextForBudget() throws Exception {
    OrdemServico os = new OrdemServico();
    os.setId(UUID.randomUUID());
    OrdemServicoResponse response = new OrdemServicoResponse();
    response.setId(os.getId());

    when(service.obterProximaParaOrcamento()).thenReturn(Optional.of(os));
    when(mapper.toResponse(os)).thenReturn(response);

    mockMvc
        .perform(get("/api/prioridade/proxima").param("tipo", "ORCAMENTO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(os.getId().toString()));
  }

  @Test
  @DisplayName("Deve obter próxima OS para execução")
  void shouldGetNextForExecution() throws Exception {
    OrdemServico os = new OrdemServico();
    os.setId(UUID.randomUUID());
    OrdemServicoResponse response = new OrdemServicoResponse();
    response.setId(os.getId());

    when(service.obterProximaParaExecucao()).thenReturn(Optional.of(os));
    when(mapper.toResponse(os)).thenReturn(response);

    mockMvc
        .perform(get("/api/prioridade/proxima").param("tipo", "EXECUCAO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(os.getId().toString()));
  }

  @Test
  @DisplayName("Deve retornar 204 quando não houver próxima OS")
  void shouldReturnNoContentWhenNoNextOS() throws Exception {
    when(service.obterProximaParaOrcamento()).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/prioridade/proxima").param("tipo", "ORCAMENTO"))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Deve atualizar prioridade da OS")
  void shouldUpdatePriority() throws Exception {
    UUID id = UUID.randomUUID();
    Prioridade novaPrioridade = Prioridade.ALTA;
    OrdemServico os = new OrdemServico();
    os.setId(id);
    os.setPrioridade(novaPrioridade);
    OrdemServicoResponse response = new OrdemServicoResponse();
    response.setId(id);
    response.setPrioridade(novaPrioridade);

    when(service.atualizarPrioridade(eq(id), eq(novaPrioridade))).thenReturn(os);
    when(mapper.toResponse(os)).thenReturn(response);

    mockMvc
        .perform(
            put("/api/prioridade/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novaPrioridade)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.prioridade").value(novaPrioridade.toString()));
  }
}
