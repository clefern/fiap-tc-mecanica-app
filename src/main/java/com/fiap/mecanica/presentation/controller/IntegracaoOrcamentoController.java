package com.fiap.mecanica.presentation.controller;

import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.infra.config.security.ActionTokenService;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infra.monitoring.MonitoredOperationType;
import com.fiap.mecanica.presentation.api.IntegracaoOrcamentoApi;
import com.fiap.mecanica.presentation.dto.AprovacaoOrcamentoExternaRequest;
import com.fiap.mecanica.presentation.dto.DecisaoOrcamento;
import com.fiap.mecanica.presentation.dto.OrcamentoResponse;
import com.fiap.mecanica.presentation.mapper.OrcamentoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/integracoes/orcamentos")
@RequiredArgsConstructor
@MonitoredOperation(type = MonitoredOperationType.APPLICATION_LATENCY_API)
public class IntegracaoOrcamentoController implements IntegracaoOrcamentoApi {

  private static final Logger log = LoggerFactory.getLogger(IntegracaoOrcamentoController.class);

  private final OrcamentoService orcamentoService;
  private final OrdemServicoRepository ordemServicoRepository;
  private final OrcamentoMapper mapper;
  private final ActionTokenService actionTokenService;

  @Override
  @PostMapping("/aprovacao")
  public ResponseEntity<OrcamentoResponse> processarAprovacaoExterna(
      @Valid @RequestBody AprovacaoOrcamentoExternaRequest request) {
    log.info(
        "[INT_001] Recebida decisão externa: codigo={} decisao={}",
        request.osCodigo(),
        request.decisao());

    OrdemServico os =
        ordemServicoRepository
            .findByCodigo(request.osCodigo())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Ordem de Serviço não encontrada: " + request.osCodigo()));

    var orcamento =
        request.decisao() == DecisaoOrcamento.APROVADO
            ? orcamentoService.aprovarPorOsId(os.getId())
            : orcamentoService.reprovarPorOsId(os.getId());

    log.info(
        "[INT_001] Decisão processada: osId={} orcamentoId={} status={}",
        os.getId(),
        orcamento.getId(),
        orcamento.getStatus());

    return ResponseEntity.ok(mapper.toResponse(orcamento));
  }

  @Override
  @GetMapping("/aprovacao")
  public ResponseEntity<OrcamentoResponse> processarAprovacaoPorToken(@RequestParam String token) {
    var payload =
        actionTokenService
            .validate(token)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Token inválido ou expirado"));

    log.info(
        "[EMAIL_001] Decisão por token: orcamentoId={} decisao={}",
        payload.orcamentoId(),
        payload.decisao());

    var orcamento =
        payload.decisao() == DecisaoOrcamento.APROVADO
            ? orcamentoService.aprovar(payload.orcamentoId())
            : orcamentoService.reprovar(payload.orcamentoId());

    log.info(
        "[EMAIL_001] Decisão processada: orcamentoId={} status={}",
        orcamento.getId(),
        orcamento.getStatus());

    return ResponseEntity.ok(mapper.toResponse(orcamento));
  }
}
