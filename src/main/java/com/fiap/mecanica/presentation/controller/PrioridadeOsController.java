package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.presentation.api.PrioridadeOsApi;
import com.fiap.mecanica.presentation.dto.OrdemServicoResponse;
import com.fiap.mecanica.presentation.mapper.OrdemServicoMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prioridade")
public class PrioridadeOsController implements PrioridadeOsApi {

  private final PrioridadeService service;
  private final OrdemServicoMapper mapper;

  public PrioridadeOsController(PrioridadeService service, OrdemServicoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Override
  @GetMapping("/fila-orcamento")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<Page<OrdemServicoResponse>> listarFilaOrcamento(Pageable pageable) {
    Page<OrdemServicoResponse> response =
        service.listarFilaOrcamento(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(response);
  }

  @Override
  @GetMapping("/fila-execucao")
  @PreAuthorize("hasAnyRole('MECANICO', 'ADMIN')")
  public ResponseEntity<Page<OrdemServicoResponse>> listarFilaExecucao(Pageable pageable) {
    Page<OrdemServicoResponse> response =
        service.listarFilaExecucao(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(response);
  }

  @Override
  @GetMapping("/proxima")
  @PreAuthorize("hasAnyRole('MECANICO', 'ADMIN')")
  public ResponseEntity<OrdemServicoResponse> obterProxima(
      @RequestParam(defaultValue = "ORCAMENTO") String tipo) {
    Optional<OrdemServico> os;
    if ("EXECUCAO".equalsIgnoreCase(tipo)) {
      os = service.obterProximaParaExecucao();
    } else {
      os = service.obterProximaParaOrcamento();
    }

    return os.map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.noContent().build());
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')") // Conforme ADR (Gerente)
  public ResponseEntity<OrdemServicoResponse> atualizarPrioridade(
      @PathVariable UUID id, @RequestBody Prioridade prioridade) {
    OrdemServico os = service.atualizarPrioridade(id, prioridade);
    return ResponseEntity.ok(mapper.toResponse(os));
  }

  // DTO interno removido em favor de Prioridade direta no corpo, conforme API.
}
