package com.fiap.mecanica.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.application.service.OrdemServicoService;
import com.fiap.mecanica.application.service.OsItemService;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.config.security.OsSecurity;
import com.fiap.mecanica.infra.config.security.UserContext;
import com.fiap.mecanica.presentation.dto.AdicionarItemRequest;
import com.fiap.mecanica.presentation.dto.AtualizarQuantidadeItemRequest;
import com.fiap.mecanica.presentation.dto.OrdemServicoRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrdemServicoControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private OrdemServicoService service;

  @MockitoBean private OsItemService itemService;

  // Security Mocks
  @MockitoBean(name = "osSecurity")
  private OsSecurity osSecurity;

  @MockitoBean private UserContext userContext;

  @MockitoBean private JavaMailSender javaMailSender;

  @Test
  @DisplayName("ADMIN: Deve criar ordem de serviço com sucesso")
  @WithMockUser(roles = "ADMIN")
  void admin_DeveCriarOrdemServico() throws Exception {
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    OrdemServicoRequest request = new OrdemServicoRequest(clienteId, veiculoId, "Observacao teste");

    OrdemServico os = OrdemServico.nova(clienteId, veiculoId);
    os.setId(UUID.randomUUID());
    os.setObservacoes("Observacao teste");

    when(service.criarOrdemServico(eq(clienteId), eq(veiculoId), eq("Observacao teste")))
        .thenReturn(os);

    mockMvc
        .perform(
            post("/api/ordens-servico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.clienteId").value(clienteId.toString()))
        .andExpect(jsonPath("$.veiculoId").value(veiculoId.toString()))
        .andExpect(jsonPath("$.status").value("RECEBIDA"));
  }

  @Test
  @DisplayName("ATENDENTE: Deve criar ordem de serviço com sucesso")
  @WithMockUser(roles = "ATENDENTE")
  void atendente_DeveCriarOrdemServico() throws Exception {
    // Reusing the same logic as admin, just ensuring role works
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    OrdemServicoRequest request = new OrdemServicoRequest(clienteId, veiculoId, "Observacao teste");

    OrdemServico os = OrdemServico.nova(clienteId, veiculoId);
    os.setId(UUID.randomUUID());
    os.setObservacoes("Observacao teste");

    when(service.criarOrdemServico(eq(clienteId), eq(veiculoId), eq("Observacao teste")))
        .thenReturn(os);

    mockMvc
        .perform(
            post("/api/ordens-servico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("CLIENTE: Nao deve criar ordem de serviço (Forbidden)")
  @WithMockUser(roles = "CLIENTE")
  void cliente_NaoDeveCriarOrdemServico() throws Exception {
    OrdemServicoRequest request =
        new OrdemServicoRequest(UUID.randomUUID(), UUID.randomUUID(), "Obs");

    mockMvc
        .perform(
            post("/api/ordens-servico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve buscar ordem de serviço por ID")
  @WithMockUser(roles = "ATENDENTE")
  void deveBuscarPorId() throws Exception {
    UUID id = UUID.randomUUID();
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(id);

    when(service.buscarPorId(id)).thenReturn(os);

    mockMvc
        .perform(get("/api/ordens-servico/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  @DisplayName("CLIENTE: Nao deve buscar ordem de serviço por ID (Forbidden)")
  @WithMockUser(roles = "CLIENTE")
  void cliente_NaoDeveBuscarPorId() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(get("/api/ordens-servico/{id}", id)).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve listar ordens de serviço")
  @WithMockUser(roles = "ADMIN")
  void deveListarOrdensServico() throws Exception {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setCodigo("OS-TEST-001");
    Page<OrdemServico> page = new PageImpl<>(List.of(os));

    when(service.listarTodas(any(), any(), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/ordens-servico"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].codigo").exists());
  }

  @Test
  @DisplayName("Deve adicionar item à ordem de serviço (MECANICO dono)")
  @WithMockUser(roles = "MECANICO")
  void deveAdicionarItem() throws Exception {
    UUID id = UUID.randomUUID();
    when(osSecurity.canWorkOn(any(), eq(id))).thenReturn(true);

    AdicionarItemRequest request =
        new AdicionarItemRequest(
            TipoItem.SERVICO, "Troca de oleo", new BigDecimal("100.00"), 1, UUID.randomUUID());

    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(id);
    // Simulating the item was added
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Troca de oleo")
            .valorUnitario(new BigDecimal("100.00"))
            .quantidade(1)
            .build();
    os.adicionarItem(item);

    when(itemService.adicionarItem(eq(id), any(ItemOrdemServico.class), any())).thenReturn(os);

    mockMvc
        .perform(
            post("/api/ordens-servico/{id}/itens", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valorTotal").value(100.0));
  }

  @Test
  @DisplayName("Nao deve adicionar item se nao for dono (MECANICO outro)")
  @WithMockUser(roles = "MECANICO")
  void naoDeveAdicionarItemSeNaoDono() throws Exception {
    UUID id = UUID.randomUUID();
    when(osSecurity.canWorkOn(any(), eq(id))).thenReturn(false);

    AdicionarItemRequest request =
        new AdicionarItemRequest(
            TipoItem.SERVICO, "Troca de oleo", new BigDecimal("100.00"), 1, UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/ordens-servico/{id}/itens", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve adicionar itens em lote à ordem de serviço (MECANICO dono)")
  @WithMockUser(roles = "MECANICO")
  void deveAdicionarItensEmLote() throws Exception {
    UUID id = UUID.randomUUID();
    when(osSecurity.canWorkOn(any(), eq(id))).thenReturn(true);

    AdicionarItemRequest request1 =
        new AdicionarItemRequest(
            TipoItem.SERVICO, "Troca de oleo", new BigDecimal("100.00"), 1, UUID.randomUUID());
    AdicionarItemRequest request2 =
        new AdicionarItemRequest(
            TipoItem.PECA, "Filtro", new BigDecimal("50.00"), 1, UUID.randomUUID());

    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(id);
    // Simulating result
    os.setValorTotal(new BigDecimal("150.00"));

    when(itemService.adicionarItensEmLote(eq(id), any(), any())).thenReturn(os);

    mockMvc
        .perform(
            post("/api/ordens-servico/{id}/itens/bulking", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request1, request2))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valorTotal").value(150.0));
  }

  @Test
  @DisplayName("Deve remover item da ordem de serviço")
  @WithMockUser(roles = "MECANICO")
  void deveRemoverItem() throws Exception {
    UUID id = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(id);

    when(osSecurity.canWorkOn(any(), eq(id))).thenReturn(true);
    when(itemService.removerItem(eq(id), eq(itemId), any())).thenReturn(os);

    mockMvc
        .perform(delete("/api/ordens-servico/{id}/itens/{itemId}", id, itemId))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Nao deve remover item se nao for dono (MECANICO outro)")
  @WithMockUser(roles = "MECANICO")
  void naoDeveRemoverItemSeNaoDono() throws Exception {
    UUID id = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();

    when(osSecurity.canWorkOn(any(), eq(id))).thenReturn(false);

    mockMvc
        .perform(delete("/api/ordens-servico/{id}/itens/{itemId}", id, itemId))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve atualizar quantidade do item da ordem de serviço")
  @WithMockUser(roles = "MECANICO")
  void deveAtualizarQuantidadeItem() throws Exception {
    UUID id = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    when(osSecurity.canWorkOn(any(), eq(id))).thenReturn(true);

    AtualizarQuantidadeItemRequest request = new AtualizarQuantidadeItemRequest(3);

    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(id);

    when(itemService.atualizarQuantidadeItem(eq(id), eq(itemId), eq(3), any())).thenReturn(os);

    mockMvc
        .perform(
            patch("/api/ordens-servico/{id}/itens/{itemId}", id, itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  @DisplayName("Nao deve atualizar quantidade se nao for dono (MECANICO outro)")
  @WithMockUser(roles = "MECANICO")
  void naoDeveAtualizarQuantidadeSeNaoDono() throws Exception {
    UUID id = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    when(osSecurity.canWorkOn(any(), eq(id))).thenReturn(false);

    AtualizarQuantidadeItemRequest request = new AtualizarQuantidadeItemRequest(2);

    mockMvc
        .perform(
            patch("/api/ordens-servico/{id}/itens/{itemId}", id, itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Nao deve adicionar itens em lote se nao for dono (MECANICO outro)")
  @WithMockUser(roles = "MECANICO")
  void naoDeveAdicionarItensEmLoteSeNaoDono() throws Exception {
    UUID id = UUID.randomUUID();
    when(osSecurity.canWorkOn(any(), eq(id))).thenReturn(false);

    AdicionarItemRequest request =
        new AdicionarItemRequest(
            TipoItem.SERVICO, "Troca de oleo", new BigDecimal("100.00"), 1, UUID.randomUUID());

    mockMvc
        .perform(
            post("/api/ordens-servico/{id}/itens/bulking", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve listar fila operacional com sucesso")
  @WithMockUser(roles = "ATENDENTE")
  void deveListarFilaOperacionalComSucesso() throws Exception {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    Page<OrdemServico> page = new PageImpl<>(List.of(os));
    when(service.listarFilaOperacional(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/ordens-servico/fila-operacional").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("Deve retornar status da OS para ATENDENTE")
  @WithMockUser(roles = "ATENDENTE")
  void deveRetornarStatusDaOs() throws Exception {
    UUID id = UUID.randomUUID();
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(id);
    os.setCodigo("OS-2025-001");

    when(service.buscarPorId(id)).thenReturn(os);

    mockMvc
        .perform(get("/api/ordens-servico/{id}/status", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.codigo").value("OS-2025-001"))
        .andExpect(jsonPath("$.status").value(StatusOS.RECEBIDA.name()))
        .andExpect(jsonPath("$.statusDescricao").value("Recebida"))
        .andExpect(jsonPath("$.dataEntrada").exists());
  }

  @Test
  @DisplayName("CLIENTE deve consultar status da OS")
  @WithMockUser(roles = "CLIENTE")
  void cliente_DeveConsultarStatusDaOs() throws Exception {
    UUID id = UUID.randomUUID();
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(id);

    when(service.buscarPorId(id)).thenReturn(os);

    mockMvc
        .perform(get("/api/ordens-servico/{id}/status", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(StatusOS.RECEBIDA.name()));
  }

  @Test
  @DisplayName("Deve retornar 404 ao buscar status de OS inexistente")
  @WithMockUser(roles = "ATENDENTE")
  void deveRetornar404ParaStatusDeOsInexistente() throws Exception {
    UUID id = UUID.randomUUID();
    when(service.buscarPorId(id)).thenThrow(new OrdemServicoNaoEncontradaException(id));

    mockMvc.perform(get("/api/ordens-servico/{id}/status", id)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve buscar OS por código com sucesso")
  @WithMockUser(roles = "ATENDENTE")
  void deveBuscarPorCodigo() throws Exception {
    String codigo = "OS-ABCD1234";
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(UUID.randomUUID());
    os.setCodigo(codigo);

    when(service.buscarPorCodigo(codigo)).thenReturn(os);

    mockMvc
        .perform(get("/api/ordens-servico/codigo/{codigo}", codigo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.codigo").value(codigo));
  }

  @Test
  @DisplayName("Deve retornar 404 ao buscar OS por código inexistente")
  @WithMockUser(roles = "ATENDENTE")
  void deveRetornar404QuandoCodigoOsNaoEncontrado() throws Exception {
    String codigo = "OS-INEXISTENTE";
    when(service.buscarPorCodigo(codigo)).thenThrow(new OrdemServicoNaoEncontradaException(codigo));

    mockMvc
        .perform(get("/api/ordens-servico/codigo/{codigo}", codigo))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("CLIENTE: Não deve buscar OS por código (Forbidden)")
  @WithMockUser(roles = "CLIENTE")
  void cliente_NaoDeveBuscarPorCodigo() throws Exception {
    mockMvc
        .perform(get("/api/ordens-servico/codigo/OS-ABCD1234"))
        .andExpect(status().isForbidden());
  }
}
