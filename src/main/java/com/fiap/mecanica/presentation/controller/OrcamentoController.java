package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.presentation.api.OrcamentoApi;
import com.fiap.mecanica.presentation.dto.OrcamentoResponse;
import com.fiap.mecanica.presentation.mapper.OrcamentoMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController implements OrcamentoApi {

  private final OrcamentoService orcamentoService;
  private final OrcamentoMapper mapper;

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN', 'CLIENTE')")
  public ResponseEntity<OrcamentoResponse> buscarPorId(@PathVariable UUID id) {
    return orcamentoService
        .buscarPorId(id)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @GetMapping("/codigo/{codigo}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN', 'CLIENTE')")
  public ResponseEntity<OrcamentoResponse> buscarPorCodigo(@PathVariable String codigo) {
    return orcamentoService
        .buscarPorCodigo(codigo)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @GetMapping("/os/{ordemServicoId}")
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN', 'CLIENTE')")
  public ResponseEntity<OrcamentoResponse> buscarPorOrdemServico(
      @PathVariable UUID ordemServicoId) {
    return orcamentoService
        .buscarPorOrdemServico(ordemServicoId)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  @GetMapping
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<Page<OrcamentoResponse>> listarTodos(
      @PageableDefault(size = 10, sort = "dataEmissao") Pageable pageable) {
    Page<OrcamentoResponse> responses =
        orcamentoService.listarTodos(pageable).map(mapper::toResponse);
    return ResponseEntity.ok(responses);
  }

  @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO', 'ADMIN')")
  public ResponseEntity<byte[]> gerarPdf(@PathVariable UUID id) {
    byte[] pdfBytes = orcamentoService.recuperarPdf(id);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"orcamento-" + id + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdfBytes);
  }

  @Override
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deletar(@PathVariable UUID id) {
    orcamentoService.deletar(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  @PostMapping("/{id}/aprovar")
  @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
  public ResponseEntity<OrcamentoResponse> aprovar(@PathVariable UUID id) {
    return ResponseEntity.ok(mapper.toResponse(orcamentoService.aprovar(id)));
  }

  @Override
  @PostMapping("/{id}/reprovar")
  @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
  public ResponseEntity<OrcamentoResponse> reprovar(@PathVariable UUID id) {
    return ResponseEntity.ok(mapper.toResponse(orcamentoService.reprovar(id)));
  }

  @Override
  @PostMapping("/os/{osId}/aprovar")
  @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
  public ResponseEntity<OrcamentoResponse> aprovarPorOsId(@PathVariable UUID osId) {
    return ResponseEntity.ok(mapper.toResponse(orcamentoService.aprovarPorOsId(osId)));
  }

  @Override
  @PostMapping("/os/{osId}/reprovar")
  @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
  public ResponseEntity<OrcamentoResponse> reprovarPorOsId(@PathVariable UUID osId) {
    return ResponseEntity.ok(mapper.toResponse(orcamentoService.reprovarPorOsId(osId)));
  }
}
