package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.OsLifecycleService;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.config.security.UserContext;
import com.fiap.mecanica.presentation.api.OrdemServicoAcoesApi;
import com.fiap.mecanica.presentation.dto.TrocarMecanicoRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordens-servico/{id}/acoes")
public class OrdemServicoAcoesController implements OrdemServicoAcoesApi {

  private final OsLifecycleService lifecycleService;
  private final UserContext userContext;

  public OrdemServicoAcoesController(OsLifecycleService lifecycleService, UserContext userContext) {
    this.lifecycleService = lifecycleService;
    this.userContext = userContext;
  }

  @Override
  @PostMapping("/trocar-mecanico")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<OrdemServico> trocarMecanico(
      @PathVariable UUID id, @RequestBody TrocarMecanicoRequest request) {
    return ResponseEntity.ok(
        lifecycleService.trocarMecanicoResponsavel(id, request.novoMecanicoId()));
  }

  @Override
  @PostMapping("/iniciar-diagnostico")
  @PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
  public ResponseEntity<OrdemServico> iniciarDiagnostico(@PathVariable UUID id) {
    return ResponseEntity.ok(
        lifecycleService.iniciarDiagnostico(id, userContext.getAuthenticatedUserId()));
  }

  @Override
  @PostMapping("/emitir-orcamento")
  @PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
  public ResponseEntity<OrdemServico> finalizarDiagnostico(@PathVariable UUID id) {
    return ResponseEntity.ok(
        lifecycleService.finalizarDiagnostico(id, userContext.getAuthenticatedUserId()));
  }

  @Override
  @PostMapping("/iniciar-execucao")
  @PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
  public ResponseEntity<OrdemServico> iniciarExecucao(@PathVariable UUID id) {
    return ResponseEntity.ok(
        lifecycleService.iniciarExecucao(id, userContext.getAuthenticatedUserId()));
  }

  @Override
  @PostMapping("/finalizar")
  @PreAuthorize("hasRole('MECANICO') and @osSecurity.canWorkOn(authentication, #id)")
  public ResponseEntity<OrdemServico> finalizar(@PathVariable UUID id) {
    return ResponseEntity.ok(lifecycleService.finalizar(id, userContext.getAuthenticatedUserId()));
  }

  @Override
  @PostMapping("/entregar")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
  public ResponseEntity<OrdemServico> entregar(@PathVariable UUID id) {
    return ResponseEntity.ok(lifecycleService.entregar(id));
  }

  @Override
  @PostMapping("/cancelar")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN') or @osSecurity.canManage(authentication, #id)")
  public ResponseEntity<OrdemServico> cancelar(@PathVariable UUID id) {
    return ResponseEntity.ok(lifecycleService.cancelar(id));
  }
}
