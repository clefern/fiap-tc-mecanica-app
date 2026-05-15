package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.OrdemServicoService;
import com.fiap.mecanica.application.service.OsItemService;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.config.security.UserContext;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.OrdemServicoApi;
import com.fiap.mecanica.presentation.dto.AberturaOsCompletaRequest;
import com.fiap.mecanica.presentation.dto.AdicionarItemRequest;
import com.fiap.mecanica.presentation.dto.AtualizarQuantidadeItemRequest;
import com.fiap.mecanica.presentation.dto.OrdemServicoRequest;
import com.fiap.mecanica.presentation.dto.OrdemServicoResponse;
import com.fiap.mecanica.presentation.dto.StatusOsResponse;
import com.fiap.mecanica.presentation.mapper.OrdemServicoMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/ordens-servico")
@SecurityRequirement(name = "bearerAuth")
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class OrdemServicoController implements OrdemServicoApi {

  private final OrdemServicoService service;
  private final OsItemService itemService;
  private final OrdemServicoMapper mapper;
  private final UserContext userContext;

  public OrdemServicoController(
      OrdemServicoService service,
      OsItemService itemService,
      OrdemServicoMapper mapper,
      UserContext userContext) {
    this.service = service;
    this.itemService = itemService;
    this.mapper = mapper;
    this.userContext = userContext;
  }

  @Override
  @PostMapping("/abertura-completa")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<OrdemServicoResponse> abrirOsCompleta(
      @RequestBody AberturaOsCompletaRequest request) {
    List<ItemOrdemServico> itens =
        request.getItens() == null
            ? null
            : request.getItens().stream().map(mapper::toDomain).toList();

    OrdemServico os =
        service.abrirOsCompleta(
            request.getClienteId(), request.getVeiculoId(), request.getObservacoes(), itens);
    OrdemServicoResponse response = mapper.toResponse(os);

    URI location =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/ordens-servico/{id}")
            .buildAndExpand(response.getId())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @Override
  @PostMapping
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<OrdemServicoResponse> criar(@RequestBody OrdemServicoRequest request) {
    OrdemServico os =
        service.criarOrdemServico(
            request.getClienteId(), request.getVeiculoId(), request.getObservacoes());
    OrdemServicoResponse response = mapper.toResponse(os);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<OrdemServicoResponse> buscarPorId(@PathVariable UUID id) {
    OrdemServico os = service.buscarPorId(id);
    return ResponseEntity.ok(mapper.toResponse(os));
  }

  @Override
  @GetMapping("/codigo/{codigo}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<OrdemServicoResponse> buscarPorCodigo(@PathVariable String codigo) {
    OrdemServico os = service.buscarPorCodigo(codigo);
    return ResponseEntity.ok(mapper.toResponse(os));
  }

  @Override
  @GetMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN', 'CLIENTE')")
  public ResponseEntity<StatusOsResponse> buscarStatus(@PathVariable UUID id) {
    OrdemServico os = service.buscarPorId(id);
    return ResponseEntity.ok(mapper.toStatusResponse(os));
  }

  @Override
  @GetMapping
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<Page<OrdemServicoResponse>> listar(
      @RequestParam(required = false) StatusOS status,
      @RequestParam(required = false) UUID clienteId,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<OrdemServicoResponse> page =
        service.listarTodas(status, clienteId, pageable).map(mapper::toResponse);
    return ResponseEntity.ok(page);
  }

  @Override
  @GetMapping("/fila-operacional")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<Page<OrdemServicoResponse>> listarFilaOperacional(
      @PageableDefault(size = 10) Pageable pageable) {
    Page<OrdemServicoResponse> page =
        service.listarFilaOperacional(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(page);
  }

  @Override
  @PostMapping("/{id}/itens")
  @PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
  public ResponseEntity<OrdemServicoResponse> adicionarItem(
      @PathVariable UUID id, @RequestBody AdicionarItemRequest request) {
    ItemOrdemServico item = mapper.toDomain(request);
    OrdemServico os = itemService.adicionarItem(id, item, userContext.getAuthenticatedUserId());
    return ResponseEntity.ok(mapper.toResponse(os));
  }

  @Override
  @PostMapping("/{id}/itens/bulking")
  @PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
  public ResponseEntity<OrdemServicoResponse> adicionarItensEmLote(
      @PathVariable UUID id, @RequestBody List<AdicionarItemRequest> requests) {
    List<ItemOrdemServico> items = requests.stream().map(mapper::toDomain).toList();
    OrdemServico os =
        itemService.adicionarItensEmLote(id, items, userContext.getAuthenticatedUserId());
    return ResponseEntity.ok(mapper.toResponse(os));
  }

  @Override
  @PatchMapping("/{id}/itens/{itemId}")
  @PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
  public ResponseEntity<OrdemServicoResponse> atualizarQuantidadeItem(
      @PathVariable UUID id,
      @PathVariable UUID itemId,
      @RequestBody AtualizarQuantidadeItemRequest request) {
    OrdemServico os =
        itemService.atualizarQuantidadeItem(
            id, itemId, request.getQuantidade(), userContext.getAuthenticatedUserId());
    return ResponseEntity.ok(mapper.toResponse(os));
  }

  @Override
  @DeleteMapping("/{id}/itens/{itemId}")
  @PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
  public ResponseEntity<OrdemServicoResponse> removerItem(
      @PathVariable UUID id, @PathVariable UUID itemId) {
    OrdemServico os = itemService.removerItem(id, itemId, userContext.getAuthenticatedUserId());
    return ResponseEntity.ok(mapper.toResponse(os));
  }
}
