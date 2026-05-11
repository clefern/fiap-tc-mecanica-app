package com.fiap.mecanica.presentation.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.application.service.estoque.EstoqueService;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.infra.config.security.JwtService;
import com.fiap.mecanica.presentation.dto.AtualizarParametrosEstoqueRequest;
import com.fiap.mecanica.presentation.dto.BaixaEstoqueRequest;
import com.fiap.mecanica.presentation.dto.EntradaEstoqueRequest;
import com.fiap.mecanica.presentation.mapper.InsumoMapper;
import com.fiap.mecanica.presentation.mapper.PecaMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EstoqueController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({PecaMapper.class, InsumoMapper.class})
class EstoqueControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private EstoqueService estoqueService;

  @MockBean private JwtService jwtService;

  @MockBean private UserDetailsService userDetailsService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("Deve retornar peça atualizada ao baixar estoque")
  void shouldReturnUpdatedPecaOnBaixaEstoque() throws Exception {
    UUID pecaId = UUID.randomUUID();
    BaixaEstoqueRequest request = new BaixaEstoqueRequest(pecaId, TipoItem.PECA, 5);

    Peca peca =
        new Peca(
            pecaId,
            "Pastilha",
            "Desc",
            new BigDecimal("100.00"),
            true,
            "Fab",
            "Cod",
            "Mod",
            10,
            5,
            50);

    when(estoqueService.baixarEstoque(eq(pecaId), eq(TipoItem.PECA), eq(5))).thenReturn(peca);

    mockMvc
        .perform(
            post("/api/estoque/baixa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(pecaId.toString()))
        .andExpect(jsonPath("$.nome").value("Pastilha"))
        .andExpect(jsonPath("$.quantidadeEstoque").value(10));
  }

  @Test
  @DisplayName("Deve retornar insumo atualizado ao registrar entrada de estoque")
  void shouldReturnUpdatedInsumoOnEntradaEstoque() throws Exception {
    UUID insumoId = UUID.randomUUID();
    EntradaEstoqueRequest request = new EntradaEstoqueRequest(insumoId, TipoItem.INSUMO, 20);

    Insumo insumo =
        new Insumo(insumoId, "Óleo", "Desc", new BigDecimal("50.00"), true, "LITRO", 120, 20, 200);

    when(estoqueService.adicionarEstoque(eq(insumoId), eq(TipoItem.INSUMO), eq(20)))
        .thenReturn(insumo);

    mockMvc
        .perform(
            post("/api/estoque/entrada")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(insumoId.toString()))
        .andExpect(jsonPath("$.nome").value("Óleo"))
        .andExpect(jsonPath("$.quantidadeEstoque").value(120));
  }

  @Test
  @DisplayName("Deve retornar peça com parâmetros de estoque atualizados")
  void shouldReturnUpdatedPecaOnAtualizarParametros() throws Exception {
    UUID pecaId = UUID.randomUUID();
    AtualizarParametrosEstoqueRequest request =
        new AtualizarParametrosEstoqueRequest(pecaId, TipoItem.PECA, 10, 80);

    Peca peca =
        new Peca(
            pecaId,
            "Pastilha",
            "Desc",
            new BigDecimal("100.00"),
            true,
            "Fab",
            "Cod",
            "Mod",
            30,
            10,
            80);

    when(estoqueService.atualizarParametrosEstoque(eq(pecaId), eq(TipoItem.PECA), eq(10), eq(80)))
        .thenReturn(peca);

    mockMvc
        .perform(
            put("/api/estoque/parametros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(pecaId.toString()))
        .andExpect(jsonPath("$.estoqueMinimo").value(10))
        .andExpect(jsonPath("$.estoqueMaximo").value(80));
  }

  @Test
  @DisplayName("Deve retornar corpo vazio quando tipo de item desconhecido")
  void shouldReturnEmptyBodyWhenUnknownItemType() throws Exception {
    UUID id = UUID.randomUUID();
    BaixaEstoqueRequest request = new BaixaEstoqueRequest(id, TipoItem.PECA, 5);

    // Mock returning a plain ItemEstocavel (mock) which is neither Peca nor Insumo
    com.fiap.mecanica.domain.model.ItemEstocavel unknownItem =
        org.mockito.Mockito.mock(com.fiap.mecanica.domain.model.ItemEstocavel.class);

    when(estoqueService.baixarEstoque(eq(id), eq(TipoItem.PECA), eq(5))).thenReturn(unknownItem);

    mockMvc
        .perform(
            post("/api/estoque/baixa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").doesNotExist());
  }
}
