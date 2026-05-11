package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.ServicoService;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.presentation.api.ServicoApi;
import com.fiap.mecanica.presentation.dto.ServicoRequest;
import com.fiap.mecanica.presentation.dto.ServicoResponse;
import com.fiap.mecanica.presentation.mapper.ServicoMapper;
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
@RequestMapping("/api/servicos")
public class ServicoController implements ServicoApi {

  private final ServicoService service;
  private final ServicoMapper mapper;

  public ServicoController(ServicoService service, ServicoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Override
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ServicoResponse> create(@RequestBody @Valid ServicoRequest request) {
    Servico servico = mapper.toDomain(request);
    Servico saved = service.create(servico);
    ServicoResponse response = mapper.toResponse(saved);
    return ResponseEntity.created(URI.create("/api/servicos/" + response.getId())).body(response);
  }

  @Override
  @GetMapping
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<Page<ServicoResponse>> getAll(
      @PageableDefault(size = 10, sort = "descricao") Pageable pageable) {
    Page<ServicoResponse> responses = service.getAll(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/ativos")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<Page<ServicoResponse>> getAllAtivos(
      @PageableDefault(size = 10, sort = "descricao") Pageable pageable) {
    Page<ServicoResponse> responses = service.getAllAtivos(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<ServicoResponse> getById(@PathVariable UUID id) {
    return service
        .getById(id)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ServicoResponse> update(
      @PathVariable UUID id, @RequestBody @Valid ServicoRequest request) {
    Servico servico = mapper.toDomain(request);
    Servico updated = service.update(id, servico);
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
