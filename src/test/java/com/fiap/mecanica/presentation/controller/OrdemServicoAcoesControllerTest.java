package com.fiap.mecanica.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.application.service.OsLifecycleService;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrdemServicoAcoesControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OsLifecycleService lifecycleService;

  @MockitoBean private OrcamentoService orcamentoService;

  @MockitoBean(name = "osSecurity")
  private com.fiap.mecanica.infra.config.security.OsSecurity osSecurity;

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired private ObjectMapper objectMapper;

  private OrdemServico osMock;
  private UUID osId;

  @BeforeEach
  void setUp() {
    osId = UUID.randomUUID();
    osMock = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    osMock.setId(osId);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void trocarMecanico_DeveRetornarSucesso_QuandoUsuarioAdmin() throws Exception {
    UUID novoMecanicoId = UUID.randomUUID();
    osMock.setId(osId);

    when(lifecycleService.trocarMecanicoResponsavel(osId, novoMecanicoId)).thenReturn(osMock);

    mockMvc
        .perform(
            post("/api/ordens-servico/{id}/acoes/trocar-mecanico", osId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        java.util.Map.of("novoMecanicoId", novoMecanicoId.toString()))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(osId.toString()));
  }

  @Test
  @WithMockUser(roles = "MECANICO")
  void trocarMecanico_DeveRetornarForbidden_QuandoUsuarioNaoAdmin() throws Exception {
    UUID novoMecanicoId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/api/ordens-servico/{id}/acoes/trocar-mecanico", osId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        java.util.Map.of("novoMecanicoId", novoMecanicoId.toString()))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "MECANICO")
  void iniciarDiagnostico_DeveRetornarSucesso_QuandoUsuarioMecanico() throws Exception {
    osMock.setStatus(StatusOS.EM_DIAGNOSTICO);
    when(lifecycleService.iniciarDiagnostico(any(UUID.class), any())).thenReturn(osMock);
    when(osSecurity.canWorkOn(any(), any())).thenReturn(true);

    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/iniciar-diagnostico", osId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void iniciarDiagnostico_DeveRetornarForbidden_QuandoUsuarioCliente() throws Exception {
    when(osSecurity.canWorkOn(any(), any())).thenReturn(false);
    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/iniciar-diagnostico", osId))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "MECANICO")
  void emitirOrcamento_DeveRetornarSucesso_QuandoUsuarioMecanico() throws Exception {
    osMock.setStatus(StatusOS.AGUARDANDO_APROVACAO);
    when(lifecycleService.finalizarDiagnostico(any(UUID.class), any())).thenReturn(osMock);
    when(osSecurity.canWorkOn(any(), any())).thenReturn(true);

    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/emitir-orcamento", osId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));
  }

  @Test
  @WithMockUser(roles = "MECANICO")
  void iniciarExecucao_DeveRetornarSucesso_QuandoUsuarioMecanico() throws Exception {
    osMock.setStatus(StatusOS.EM_EXECUCAO);
    when(lifecycleService.iniciarExecucao(any(UUID.class), any())).thenReturn(osMock);
    when(osSecurity.canWorkOn(any(), any())).thenReturn(true);

    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/iniciar-execucao", osId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
  }

  @Test
  @WithMockUser(roles = "MECANICO")
  void finalizar_DeveRetornarSucesso_QuandoUsuarioMecanico() throws Exception {
    osMock.setStatus(StatusOS.FINALIZADA);
    when(lifecycleService.finalizar(any(UUID.class), any())).thenReturn(osMock);
    when(osSecurity.canWorkOn(any(), any())).thenReturn(true);

    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/finalizar", osId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("FINALIZADA"));
  }

  @Test
  @WithMockUser(roles = "ATENDENTE")
  void entregar_DeveRetornarSucesso_QuandoUsuarioAtendente() throws Exception {
    osMock.setStatus(StatusOS.ENTREGUE);
    when(lifecycleService.entregar(any(UUID.class))).thenReturn(osMock);

    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/entregar", osId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ENTREGUE"));
  }

  @Test
  @WithMockUser(roles = "MECANICO")
  void entregar_DeveRetornarForbidden_QuandoUsuarioMecanico() throws Exception {
    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/entregar", osId))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void cancelar_DeveRetornarSucesso_QuandoUsuarioAdmin() throws Exception {
    osMock.setStatus(StatusOS.CANCELADA);
    when(lifecycleService.cancelar(any(UUID.class))).thenReturn(osMock);

    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/cancelar", osId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELADA"));
  }

  @Test
  @WithMockUser(roles = "MECANICO")
  void cancelar_DeveRetornarSucesso_QuandoUsuarioMecanicoDono() throws Exception {
    osMock.setStatus(StatusOS.CANCELADA);
    when(lifecycleService.cancelar(any(UUID.class))).thenReturn(osMock);
    when(osSecurity.canManage(any(), any())).thenReturn(true);

    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/cancelar", osId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELADA"));
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void cancelar_DeveRetornarForbidden_QuandoUsuarioCliente() throws Exception {
    when(osSecurity.canManage(any(), any())).thenReturn(false);
    mockMvc
        .perform(post("/api/ordens-servico/{id}/acoes/cancelar", osId))
        .andExpect(status().isForbidden());
  }
}
