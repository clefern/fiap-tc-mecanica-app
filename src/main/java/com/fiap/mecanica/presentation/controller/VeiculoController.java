package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.VeiculoService;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.presentation.api.VeiculoApi;
import com.fiap.mecanica.presentation.dto.VeiculoRequest;
import com.fiap.mecanica.presentation.dto.VeiculoResponse;
import com.fiap.mecanica.presentation.mapper.VeiculoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VeiculoController implements VeiculoApi {

  private final VeiculoService veiculoService;
  private final VeiculoMapper mapper;

  public VeiculoController(VeiculoService veiculoService, VeiculoMapper mapper) {
    this.veiculoService = veiculoService;
    this.mapper = mapper;
  }

  @Override
  @PostMapping("/api/clientes/{clienteId}/veiculos")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
  public ResponseEntity<VeiculoResponse> create(
      @PathVariable UUID clienteId, @RequestBody @Valid VeiculoRequest request) {
    Veiculo v = mapper.toDomain(request);
    Veiculo saved = veiculoService.create(clienteId, v);
    VeiculoResponse response = mapper.toResponse(saved);
    return ResponseEntity.created(URI.create("/api/veiculos/" + response.getPlaca()))
        .body(response);
  }

  @Override
  @GetMapping("/api/veiculos/{placa}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<VeiculoResponse> getByPlaca(@PathVariable String placa) {
    return veiculoService
        .getByPlaca(placa)
        .map(veiculo -> ResponseEntity.ok(mapper.toResponse(veiculo)))
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @DeleteMapping("/api/veiculos/{placa}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteByPlaca(@PathVariable String placa) {
    veiculoService.deleteByPlaca(placa);
    return ResponseEntity.noContent().build();
  }

  @Override
  @GetMapping("/api/clientes/{clienteId}/veiculos")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
  public ResponseEntity<List<VeiculoResponse>> listByCliente(@PathVariable UUID clienteId) {
    List<VeiculoResponse> list =
        veiculoService.listByClienteId(clienteId).stream().map(mapper::toResponse).toList();
    return ResponseEntity.ok(list);
  }
}
