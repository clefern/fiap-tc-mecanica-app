package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.AtendenteService;
import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.AtendenteApi;
import com.fiap.mecanica.presentation.dto.AtendenteRequest;
import com.fiap.mecanica.presentation.dto.AtendenteResponse;
import com.fiap.mecanica.presentation.mapper.AtendenteMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/atendentes")
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class AtendenteController implements AtendenteApi {

  private final AtendenteService service;
  private final AtendenteMapper mapper;

  public AtendenteController(AtendenteService service, AtendenteMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Override
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AtendenteResponse> create(@RequestBody @Valid AtendenteRequest request) {
    Atendente novo = mapper.toDomain(request);
    Atendente salvo = service.create(novo);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(salvo.getId())
            .toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AtendenteResponse> getById(@PathVariable UUID id) {
    return service
        .getById(id)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @GetMapping("/cpf/{cpf}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AtendenteResponse> getByCpf(@PathVariable String cpf) {
    return service
        .getByCpf(cpf)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AtendenteResponse> update(
      @PathVariable UUID id, @RequestBody @Valid AtendenteRequest request) {
    Atendente atendente = mapper.toDomain(request);
    Atendente updated = service.update(id, atendente);
    return ResponseEntity.ok(mapper.toResponse(updated));
  }

  @Override
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AtendenteResponse>> getAll(
      @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    Page<Atendente> atendentes = service.getAll(pageable);
    return ResponseEntity.ok(atendentes.map(mapper::toResponse));
  }
}
