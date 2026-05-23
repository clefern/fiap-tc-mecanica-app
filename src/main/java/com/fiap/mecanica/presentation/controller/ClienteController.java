package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.ClienteService;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.ClienteApi;
import com.fiap.mecanica.presentation.dto.ClienteRequest;
import com.fiap.mecanica.presentation.dto.ClienteResponse;
import com.fiap.mecanica.presentation.mapper.ClienteMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;
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

@RestController
@RequestMapping("/api/clientes")
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class ClienteController implements ClienteApi {

  private final ClienteService clienteService;
  private final ClienteMapper mapper;

  public ClienteController(ClienteService clienteService, ClienteMapper mapper) {
    this.clienteService = clienteService;
    this.mapper = mapper;
  }

  @Override
  @PostMapping
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
  public ResponseEntity<ClienteResponse> create(@RequestBody @Valid ClienteRequest request) {
    Cliente cliente = mapper.toDomain(request);
    Cliente saved = clienteService.create(cliente);
    ClienteResponse response = mapper.toResponse(saved);
    return ResponseEntity.created(URI.create("/api/clientes/" + response.getId())).body(response);
  }

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<ClienteResponse> getById(@PathVariable UUID id) {
    Optional<Cliente> found = clienteService.getById(id);
    return found
        .map(cliente -> ResponseEntity.ok(mapper.toResponse(cliente)))
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @GetMapping("/documento/{documento}")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'ATENDENTE') or (hasRole('CLIENTE') and"
          + " @securityService.isOwnerByDocumento(authentication, #documento))")
  public ResponseEntity<ClienteResponse> getByDocumento(@PathVariable String documento) {
    Optional<Cliente> found = clienteService.getByDocumento(documento);
    return found
        .map(cliente -> ResponseEntity.ok(mapper.toResponse(cliente)))
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')") // Apenas staff pode listar tudo
  public ResponseEntity<Page<ClienteResponse>> getAll(
      @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
    Page<ClienteResponse> page = clienteService.getAll(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(page);
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
  public ResponseEntity<ClienteResponse> update(
      @PathVariable UUID id, @RequestBody @Valid ClienteRequest request) {
    Cliente cliente = mapper.toDomain(request);
    Cliente updated = clienteService.update(id, cliente);
    return ResponseEntity.ok(mapper.toResponse(updated));
  }

  @Override
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    clienteService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
