package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.MecanicoService;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.presentation.api.MecanicoApi;
import com.fiap.mecanica.presentation.dto.MecanicoRequest;
import com.fiap.mecanica.presentation.dto.MecanicoResponse;
import com.fiap.mecanica.presentation.mapper.MecanicoMapper;
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
@RequestMapping("/api/mecanicos")
public class MecanicoController implements MecanicoApi {

  private final MecanicoService service;
  private final MecanicoMapper mapper;

  public MecanicoController(MecanicoService service, MecanicoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Override
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MecanicoResponse> create(@RequestBody @Valid MecanicoRequest request) {
    Mecanico novo = mapper.toDomain(request);
    Mecanico salvo = service.create(novo);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(salvo.getId())
            .toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
  public ResponseEntity<MecanicoResponse> getById(@PathVariable UUID id) {
    return service
        .getById(id)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @GetMapping("/cpf/{cpf}")
  @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
  public ResponseEntity<MecanicoResponse> getByCpf(@PathVariable String cpf) {
    return service
        .getByCpf(cpf)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
  public ResponseEntity<Page<MecanicoResponse>> getAll(
      @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    return ResponseEntity.ok(service.getAll(pageable).map(mapper::toResponse));
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MecanicoResponse> update(
      @PathVariable UUID id, @RequestBody @Valid MecanicoRequest request) {
    Mecanico atualizado = mapper.toDomain(request);
    Mecanico salvo = service.update(id, atualizado);
    return ResponseEntity.ok(mapper.toResponse(salvo));
  }

  @Override
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
