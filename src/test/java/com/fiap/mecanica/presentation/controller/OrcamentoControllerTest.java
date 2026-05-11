package com.fiap.mecanica.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.presentation.dto.OrcamentoResponse;
import com.fiap.mecanica.presentation.mapper.OrcamentoMapper;
import java.util.List;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrcamentoControllerTest {

  private MockMvc mockMvc;

  @Mock private OrcamentoService orcamentoService;

  @Mock private OrcamentoMapper mapper;

  @InjectMocks private OrcamentoController orcamentoController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(orcamentoController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  @DisplayName("Deve buscar orçamento por ID com sucesso")
  void deveBuscarPorId() throws Exception {
    UUID id = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(id).build();

    when(orcamentoService.buscarPorId(id)).thenReturn(Optional.of(orcamento));
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(get("/api/orcamentos/{id}", id).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  @DisplayName("Deve retornar 404 quando orçamento não encontrado por ID")
  void deveRetornar404QuandoNaoEncontradoPorId() throws Exception {
    UUID id = UUID.randomUUID();
    when(orcamentoService.buscarPorId(id)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/orcamentos/{id}", id).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve buscar orçamento por ID da Ordem de Serviço")
  void deveBuscarPorOrdemServicoId() throws Exception {
    UUID osId = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().ordemServicoId(osId).build();

    when(orcamentoService.buscarPorOrdemServico(osId)).thenReturn(Optional.of(orcamento));
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(get("/api/orcamentos/os/{id}", osId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ordemServicoId").value(osId.toString()));
  }

  @Test
  @DisplayName("Deve retornar 404 quando orçamento não encontrado por ID da Ordem de Serviço")
  void deveRetornar404QuandoNaoEncontradoPorOrdemServicoId() throws Exception {
    UUID osId = UUID.randomUUID();
    when(orcamentoService.buscarPorOrdemServico(osId)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/orcamentos/os/{id}", osId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve gerar PDF do orçamento")
  void deveGerarPdfDoOrcamento() throws Exception {
    UUID id = UUID.randomUUID();
    byte[] pdfContent = "PDF CONTENT".getBytes();

    when(orcamentoService.recuperarPdf(id)).thenReturn(pdfContent);

    mockMvc
        .perform(get("/api/orcamentos/{id}/pdf", id).accept(MediaType.APPLICATION_PDF))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(content().bytes(pdfContent))
        .andExpect(
            header()
                .string(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"orcamento-" + id + ".pdf\""));
  }

  @Test
  @DisplayName("Deve listar orçamentos paginados")
  void deveListarPaginado() throws Exception {
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response =
        OrcamentoResponse.builder().id(UUID.randomUUID()).codigo("ORC-001").build();
    Page<Orcamento> page =
        new PageImpl<>(
            List.of(orcamento), org.springframework.data.domain.PageRequest.of(0, 10), 1);

    when(orcamentoService.listarTodos(any(Pageable.class))).thenReturn(page);
    when(mapper.toResponse(any(Orcamento.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/orcamentos").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].codigo").value("ORC-001"));
  }

  @Test
  @DisplayName("Deve deletar orçamento por ID")
  void deveDeletarPorId() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/orcamentos/{id}", id).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Deve aprovar orçamento por ID")
  void deveAprovarOrcamentoPorId() throws Exception {
    UUID id = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(id).build();

    when(orcamentoService.aprovar(id)).thenReturn(orcamento);
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(post("/api/orcamentos/{id}/aprovar", id).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  @DisplayName("Deve reprovar orçamento por ID")
  void deveReprovarOrcamentoPorId() throws Exception {
    UUID id = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(id).build();

    when(orcamentoService.reprovar(id)).thenReturn(orcamento);
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(post("/api/orcamentos/{id}/reprovar", id).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  @DisplayName("Deve aprovar orçamento por ID da OS")
  void deveAprovarOrcamentoPorOsId() throws Exception {
    UUID osId = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(UUID.randomUUID()).build();

    when(orcamentoService.aprovarPorOsId(osId)).thenReturn(orcamento);
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(
            post("/api/orcamentos/os/{osId}/aprovar", osId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  @DisplayName("Deve reprovar orçamento por ID da OS")
  void deveReprovarOrcamentoPorOsId() throws Exception {
    UUID osId = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(UUID.randomUUID()).build();

    when(orcamentoService.reprovarPorOsId(osId)).thenReturn(orcamento);
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(
            post("/api/orcamentos/os/{osId}/reprovar", osId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  @DisplayName("Deve buscar orçamento por código com sucesso")
  void deveBuscarPorCodigo() throws Exception {
    String codigo = "ORC-ABCD1234";
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(UUID.randomUUID()).build();

    when(orcamentoService.buscarPorCodigo(codigo)).thenReturn(Optional.of(orcamento));
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(get("/api/orcamentos/codigo/{codigo}", codigo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  @DisplayName("Deve retornar 404 quando código de orçamento não existe")
  void deveRetornar404QuandoCodigoOrcamentoNaoEncontrado() throws Exception {
    String codigo = "ORC-INEXISTENTE";
    when(orcamentoService.buscarPorCodigo(codigo)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/orcamentos/codigo/{codigo}", codigo))
        .andExpect(status().isNotFound());
  }
}
