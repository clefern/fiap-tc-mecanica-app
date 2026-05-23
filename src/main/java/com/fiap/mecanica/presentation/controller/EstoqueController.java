package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.estoque.EstoqueService;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemEstocavel;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.EstoqueApi;
import com.fiap.mecanica.presentation.dto.AtualizarParametrosEstoqueRequest;
import com.fiap.mecanica.presentation.dto.BaixaEstoqueRequest;
import com.fiap.mecanica.presentation.dto.EntradaEstoqueRequest;
import com.fiap.mecanica.presentation.mapper.InsumoMapper;
import com.fiap.mecanica.presentation.mapper.PecaMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Gerenciamento de Estoque de Peças e Insumos")
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class EstoqueController implements EstoqueApi {

  private final EstoqueService estoqueService;
  private final PecaMapper pecaMapper;
  private final InsumoMapper insumoMapper;

  @PostMapping("/baixa")
  @Override
  public ResponseEntity<?> baixarEstoque(@Valid @RequestBody BaixaEstoqueRequest request) {
    log.info("Recebida solicitação de baixa de estoque via REST: {}", request);
    ItemEstocavel item =
        estoqueService.baixarEstoque(
            request.getReferenciaId(), request.getTipo(), request.getQuantidade());
    return ResponseEntity.ok(mapearItemParaResponse(item));
  }

  @PostMapping("/entrada")
  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> registrarEntradaEstoque(
      @Valid @RequestBody EntradaEstoqueRequest request) {
    log.info("Recebida solicitação de entrada de estoque via REST: {}", request);
    ItemEstocavel item =
        estoqueService.adicionarEstoque(
            request.getReferenciaId(), request.getTipo(), request.getQuantidade());
    return ResponseEntity.ok(mapearItemParaResponse(item));
  }

  @PutMapping("/parametros")
  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> atualizarParametrosEstoque(
      @Valid @RequestBody AtualizarParametrosEstoqueRequest request) {
    log.info("Recebida solicitação de atualização de parâmetros de estoque via REST: {}", request);
    ItemEstocavel item =
        estoqueService.atualizarParametrosEstoque(
            request.getReferenciaId(),
            request.getTipo(),
            request.getEstoqueMinimo(),
            request.getEstoqueMaximo());
    return ResponseEntity.ok(mapearItemParaResponse(item));
  }

  private Object mapearItemParaResponse(ItemEstocavel item) {
    if (item instanceof Peca peca) {
      return pecaMapper.toResponse(peca);
    }
    if (item instanceof Insumo insumo) {
      return insumoMapper.toResponse(insumo);
    }
    return null;
  }
}
