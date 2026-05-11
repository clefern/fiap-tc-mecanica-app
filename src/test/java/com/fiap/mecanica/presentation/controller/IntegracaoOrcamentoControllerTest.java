package com.fiap.mecanica.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.infra.config.security.ActionTokenPayload;
import com.fiap.mecanica.infra.config.security.ActionTokenService;
import com.fiap.mecanica.presentation.dto.AprovacaoOrcamentoExternaRequest;
import com.fiap.mecanica.presentation.dto.DecisaoOrcamento;
import com.fiap.mecanica.presentation.dto.OrcamentoResponse;
import com.fiap.mecanica.presentation.mapper.OrcamentoMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class IntegracaoOrcamentoControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private OrcamentoService orcamentoService;
  @Mock private OrdemServicoRepository ordemServicoRepository;
  @Mock private OrcamentoMapper mapper;
  @Mock private ActionTokenService actionTokenService;

  @InjectMocks private IntegracaoOrcamentoController controller;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  @DisplayName("Deve aprovar orçamento por código da OS com sucesso")
  void deveAprovarOrcamentoPorCodigoDaOs() throws Exception {
    String osCodigo = "OS-2025-001";
    UUID osId = UUID.randomUUID();
    UUID orcamentoId = UUID.randomUUID();

    OrdemServico os = OrdemServico.builder().id(osId).codigo(osCodigo).build();
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(orcamentoId).build();

    when(ordemServicoRepository.findByCodigo(osCodigo)).thenReturn(Optional.of(os));
    when(orcamentoService.aprovarPorOsId(osId)).thenReturn(orcamento);
    when(mapper.toResponse(orcamento)).thenReturn(response);

    var request = new AprovacaoOrcamentoExternaRequest(osCodigo, DecisaoOrcamento.APROVADO);

    mockMvc
        .perform(
            post("/api/integracoes/orcamentos/aprovacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orcamentoId.toString()));
  }

  @Test
  @DisplayName("Deve reprovar orçamento por código da OS com sucesso")
  void deveReprovarOrcamentoPorCodigoDaOs() throws Exception {
    String osCodigo = "OS-2025-002";
    UUID osId = UUID.randomUUID();
    UUID orcamentoId = UUID.randomUUID();

    OrdemServico os = OrdemServico.builder().id(osId).codigo(osCodigo).build();
    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(orcamentoId).build();

    when(ordemServicoRepository.findByCodigo(osCodigo)).thenReturn(Optional.of(os));
    when(orcamentoService.reprovarPorOsId(osId)).thenReturn(orcamento);
    when(mapper.toResponse(orcamento)).thenReturn(response);

    var request = new AprovacaoOrcamentoExternaRequest(osCodigo, DecisaoOrcamento.REPROVADO);

    mockMvc
        .perform(
            post("/api/integracoes/orcamentos/aprovacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orcamentoId.toString()));
  }

  @Test
  @DisplayName("Deve retornar 400 quando OS código não informado")
  void deveRetornar400QuandoCodigoOsNaoInformado() throws Exception {
    var request = new AprovacaoOrcamentoExternaRequest("", DecisaoOrcamento.APROVADO);

    mockMvc
        .perform(
            post("/api/integracoes/orcamentos/aprovacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Deve lançar exceção quando OS não encontrada")
  void deveLancarExcecaoQuandoOsNaoEncontrada() throws Exception {
    String osCodigo = "OS-INEXISTENTE";

    when(ordemServicoRepository.findByCodigo(osCodigo)).thenReturn(Optional.empty());

    var request = new AprovacaoOrcamentoExternaRequest(osCodigo, DecisaoOrcamento.APROVADO);

    Assertions.assertThrows(
        Exception.class,
        () ->
            mockMvc.perform(
                post("/api/integracoes/orcamentos/aprovacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))));
  }

  @Test
  @DisplayName("Deve aprovar orçamento via token de email com sucesso")
  void deveAprovarOrcamentoPorTokenDeEmail() throws Exception {
    UUID orcamentoId = UUID.randomUUID();
    String token = "valid-token";

    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(orcamentoId).build();

    when(actionTokenService.validate(token))
        .thenReturn(Optional.of(new ActionTokenPayload(orcamentoId, DecisaoOrcamento.APROVADO)));
    when(orcamentoService.aprovar(orcamentoId)).thenReturn(orcamento);
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(get("/api/integracoes/orcamentos/aprovacao").param("token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orcamentoId.toString()));
  }

  @Test
  @DisplayName("Deve reprovar orçamento via token de email com sucesso")
  void deveReprovarOrcamentoPorTokenDeEmail() throws Exception {
    UUID orcamentoId = UUID.randomUUID();
    String token = "valid-token-reprovado";

    Orcamento orcamento = new Orcamento();
    OrcamentoResponse response = OrcamentoResponse.builder().id(orcamentoId).build();

    when(actionTokenService.validate(token))
        .thenReturn(Optional.of(new ActionTokenPayload(orcamentoId, DecisaoOrcamento.REPROVADO)));
    when(orcamentoService.reprovar(orcamentoId)).thenReturn(orcamento);
    when(mapper.toResponse(orcamento)).thenReturn(response);

    mockMvc
        .perform(get("/api/integracoes/orcamentos/aprovacao").param("token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orcamentoId.toString()));
  }

  @Test
  @DisplayName("Deve retornar 401 quando token de email inválido")
  void deveRetornar401QuandoTokenInvalido() throws Exception {
    when(actionTokenService.validate("bad-token")).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/integracoes/orcamentos/aprovacao").param("token", "bad-token"))
        .andExpect(status().isUnauthorized());
  }
}
