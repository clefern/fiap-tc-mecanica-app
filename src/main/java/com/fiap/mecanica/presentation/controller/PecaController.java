package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.PecaApi;
import com.fiap.mecanica.presentation.dto.PecaRequest;
import com.fiap.mecanica.presentation.dto.PecaResponse;
import com.fiap.mecanica.presentation.mapper.PecaMapper;
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
@RequestMapping("/api/pecas")
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class PecaController implements PecaApi {

  private final PecaService service;
  private final PecaMapper mapper;

  public PecaController(PecaService service, PecaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Override
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PecaResponse> create(@RequestBody @Valid PecaRequest request) {
    Peca peca = mapper.toDomain(request);
    Peca saved = service.create(peca);
    PecaResponse response = mapper.toResponse(saved);
    return ResponseEntity.created(URI.create("/api/pecas/" + response.getId())).body(response);
  }

  @Override
  @GetMapping
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<Page<PecaResponse>> getAll(
      @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    Page<PecaResponse> responses = service.getAll(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/ativas")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<Page<PecaResponse>> getAllAtivos(
      @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    Page<PecaResponse> responses = service.getAllAtivos(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<Page<PecaResponse>> search(
      String termo, @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    Page<PecaResponse> responses = service.search(termo, pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<PecaResponse> getById(@PathVariable UUID id) {
    return service
        .getById(id)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PecaResponse> update(
      @PathVariable UUID id, @RequestBody @Valid PecaRequest request) {
    Peca peca = mapper.toDomain(request);
    Peca updated = service.update(id, peca);
    return ResponseEntity.ok(mapper.toResponse(updated));
  }

  @Override
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
