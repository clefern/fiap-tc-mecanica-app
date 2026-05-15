package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.InsumoApi;
import com.fiap.mecanica.presentation.dto.InsumoRequest;
import com.fiap.mecanica.presentation.dto.InsumoResponse;
import com.fiap.mecanica.presentation.mapper.InsumoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insumos")
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class InsumoController implements InsumoApi {

  private final InsumoService service;
  private final InsumoMapper mapper;

  public InsumoController(InsumoService service, InsumoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Override
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<InsumoResponse> create(@RequestBody @Valid InsumoRequest request) {
    Insumo insumo = mapper.toDomain(request);
    Insumo saved = service.create(insumo);
    InsumoResponse response = mapper.toResponse(saved);
    return ResponseEntity.created(URI.create("/api/insumos/" + response.getId())).body(response);
  }

  @Override
  @GetMapping
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<Page<InsumoResponse>> getAll(
      @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    Page<InsumoResponse> responses = service.getAll(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/ativos")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<Page<InsumoResponse>> getAllAtivos(
      @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    Page<InsumoResponse> responses = service.getAllAtivos(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<Page<InsumoResponse>> search(
      String termo, @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    Page<InsumoResponse> responses = service.search(termo, pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<InsumoResponse> getById(@PathVariable UUID id) {
    return service
        .getById(id)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<InsumoResponse> update(
      @PathVariable UUID id, @RequestBody @Valid InsumoRequest request) {
    Insumo insumo = mapper.toDomain(request);
    Insumo updated = service.update(id, insumo);
    return ResponseEntity.ok(mapper.toResponse(updated));
  }

  @Override
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
