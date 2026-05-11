package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.events.OrcamentoAprovadoEvent;
import com.fiap.mecanica.application.events.OrcamentoGeradoEvent;
import com.fiap.mecanica.application.events.OrcamentoReprovadoEvent;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.application.service.PdfService;
import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.OrcamentoRepository;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.infrastructure.integration.estoque.EstoqueGateway;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OrcamentoServiceImpl implements OrcamentoService {

  private final OrcamentoRepository orcamentoRepository;
  private final OrdemServicoRepository ordemServicoRepository;
  private final ClienteRepository clienteRepository;
  private final VeiculoRepository veiculoRepository;
  private final PdfService pdfService;
  private final ApplicationEventPublisher eventPublisher;
  private final EstoqueGateway estoqueGateway;
  private final BigDecimal taxaImpostos;

  public OrcamentoServiceImpl(
      OrcamentoRepository orcamentoRepository,
      OrdemServicoRepository ordemServicoRepository,
      ClienteRepository clienteRepository,
      VeiculoRepository veiculoRepository,
      PdfService pdfService,
      ApplicationEventPublisher eventPublisher,
      EstoqueGateway estoqueGateway,
      @Value("${mecanica.orcamento.taxa-impostos}") BigDecimal taxaImpostos) {
    this.orcamentoRepository = orcamentoRepository;
    this.ordemServicoRepository = ordemServicoRepository;
    this.clienteRepository = clienteRepository;
    this.veiculoRepository = veiculoRepository;
    this.pdfService = pdfService;
    this.eventPublisher = eventPublisher;
    this.estoqueGateway = estoqueGateway;
    this.taxaImpostos = taxaImpostos;
  }

  @Override
  @Transactional
  @MonitoredOperation("orcamento.gerar")
  public Orcamento gerarOrcamento(OrdemServico ordemServico) {
    if (orcamentoRepository.existsByOrdemServicoIdAndStatus(
        ordemServico.getId(), StatusOrcamento.GERADO)) {
      log.info(
          "Já existe um orçamento gerado para esta OS. Vamos cancelar esse antes de gerar um"
              + " novo.");
      orcamentoRepository
          .findByOrdemServicoIdAndStatus(ordemServico.getId(), StatusOrcamento.GERADO)
          .ifPresent(
              orcamento -> {
                orcamento.cancelar();
                orcamentoRepository.save(orcamento);
              });
    }

    BigDecimal totalMateriais = calcularTotal(ordemServico, TipoItem.PECA, TipoItem.INSUMO);
    BigDecimal totalMaoDeObra = calcularTotal(ordemServico, TipoItem.SERVICO);

    BigDecimal valorImpostos = totalMaoDeObra.multiply(taxaImpostos);
    BigDecimal valorTotal = totalMateriais.add(totalMaoDeObra).add(valorImpostos);

    Orcamento orcamento =
        Orcamento.builder()
            .codigo(gerarCodigoOrcamento())
            .ordemServicoId(ordemServico.getId())
            .mecanicoDiagnosticoId(ordemServico.getMecanicoDiagnosticoId())
            .dataEmissao(LocalDateTime.now())
            .dataValidade(LocalDateTime.now().plusDays(30))
            .valorTotalMateriais(totalMateriais)
            .valorTotalMaoDeObra(totalMaoDeObra)
            .valorImpostos(valorImpostos)
            .valorTotal(valorTotal)
            .status(StatusOrcamento.GERADO)
            .build();

    orcamento = orcamentoRepository.save(orcamento);

    log.info("[ORCAMENTO_GERAR] ID={} OS={}", orcamento.getId(), ordemServico.getId());

    // Definição da estratégia de PDF: Geração sob demanda (Lazy)
    // Não geramos o binário agora para economizar recursos.
    // A URL aponta para o endpoint que gerará o PDF em tempo real quando
    // solicitado.
    String downloadUrl = "/api/orcamentos/" + orcamento.getId() + "/pdf";
    orcamento.setUrlPdf(downloadUrl);
    orcamentoRepository.save(orcamento);

    eventPublisher.publishEvent(new OrcamentoGeradoEvent(this, orcamento));

    return orcamento;
  }

  @Override
  public Optional<Orcamento> buscarPorId(UUID id) {
    return orcamentoRepository.findById(id);
  }

  @Override
  public Optional<Orcamento> buscarPorCodigo(String codigo) {
    return orcamentoRepository.findByCodigo(codigo);
  }

  @Override
  public Optional<Orcamento> buscarPorOrdemServico(UUID ordemServicoId) {
    // Tenta buscar o ativo (GERADO)
    Optional<Orcamento> ativo =
        orcamentoRepository.findByOrdemServicoIdAndStatus(ordemServicoId, StatusOrcamento.GERADO);
    if (ativo.isPresent()) {
      return ativo;
    }

    // Se não, busca o mais recente do histórico
    List<Orcamento> todos = orcamentoRepository.findAllByOrdemServicoId(ordemServicoId);
    if (todos.isEmpty()) {
      return Optional.empty();
    }

    return todos.stream().max(Comparator.comparing(Orcamento::getDataEmissao));
  }

  @Override
  @Transactional
  public void cancelarOrcamentosPendentes(UUID ordemServicoId) {
    List<Orcamento> orcamentos = orcamentoRepository.findAllByOrdemServicoId(ordemServicoId);
    orcamentos.stream()
        .filter(o -> o.getStatus() == StatusOrcamento.GERADO)
        .forEach(
            o -> {
              o.cancelar();
              orcamentoRepository.save(o);
            });
  }

  @Override
  public Page<Orcamento> listarTodos(Pageable pageable) {
    return orcamentoRepository.findAll(pageable);
  }

  @Override
  @Transactional
  public void deletar(UUID id) {
    orcamentoRepository.deleteById(id);
  }

  @Override
  @Transactional
  @MonitoredOperation("orcamento.aprovar")
  public Orcamento aprovar(UUID id) {
    log.info("[ORCAMENTO_APROVAR_INICIO] ID={}", id);
    Orcamento orcamento =
        orcamentoRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado: " + id));

    OrdemServico os =
        ordemServicoRepository
            .findByIdWithItens(orcamento.getOrdemServicoId())
            .orElseThrow(() -> new IllegalArgumentException("OS não encontrada"));
    log.info(
        "[ORCAMENTO_APROVAR_OS_CARREGADA] OS={} Itens={}",
        os.getId(),
        os.getItens() != null ? os.getItens().size() : 0);
    if (os.getItens() == null || os.getItens().isEmpty()) {
      log.warn("⚠️ [ORCAMENTO_APROVAR_ITENS_VAZIOS] OS={} Codigo={}", os.getId(), os.getCodigo());
    }
    try {
      os.getItens()
          .forEach(
              item ->
                  estoqueGateway.baixarEstoque(
                      item.getReferenciaId(), item.getTipo(), item.getQuantidade()));
    } catch (RuntimeException e) {
      log.error(
          "❌ [ORCAMENTO_APROVAR_ESTOQUE_FALHA] Falha ao processar baixa de estoque. Orcamento={}",
          id,
          e);
      throw e;
    }

    orcamento.aprovar();
    Orcamento salvo = orcamentoRepository.save(orcamento);
    log.info("[ORCAMENTO_APROVAR_SUCESSO] ID={}", salvo.getId());

    eventPublisher.publishEvent(new OrcamentoAprovadoEvent(this, salvo));

    return salvo;
  }

  @Override
  @Transactional
  @MonitoredOperation("orcamento.aprovarPorOs")
  public Orcamento aprovarPorOsId(UUID osId) {
    log.info("[ORCAMENTO_APROVAR_POR_OS] OS={}", osId);
    Orcamento orcamento =
        buscarPorOrdemServico(osId)
            .filter(o -> o.getStatus() == StatusOrcamento.GERADO)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Nenhum orçamento em estado GERADO encontrado para a OS: " + osId));
    return aprovar(orcamento.getId());
  }

  @Override
  @Transactional
  @MonitoredOperation("orcamento.reprovar")
  public Orcamento reprovar(UUID id) {
    log.info("[ORCAMENTO_REPROVAR_INICIO] ID={}", id);
    Orcamento orcamento =
        orcamentoRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado: " + id));
    orcamento.reprovar();
    Orcamento salvo = orcamentoRepository.save(orcamento);

    log.info("[ORCAMENTO_REPROVAR_SUCESSO] ID={}", salvo.getId());

    eventPublisher.publishEvent(new OrcamentoReprovadoEvent(this, salvo));

    return salvo;
  }

  @Override
  @Transactional
  @MonitoredOperation("orcamento.reprovarPorOs")
  public Orcamento reprovarPorOsId(UUID osId) {
    log.info("[ORCAMENTO_REPROVAR_POR_OS] OS={}", osId);
    Orcamento orcamento =
        buscarPorOrdemServico(osId)
            .filter(o -> o.getStatus() == StatusOrcamento.GERADO)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Nenhum orçamento em estado GERADO encontrado para a OS: " + osId));
    return reprovar(orcamento.getId());
  }

  @Override
  @Transactional
  @MonitoredOperation("orcamento.cancelar")
  public Orcamento cancelar(UUID id) {
    log.info("[ORCAMENTO_CANCELAR_INICIO] ID={}", id);
    Orcamento orcamento =
        orcamentoRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado: " + id));
    orcamento.cancelar();
    Orcamento salvo = orcamentoRepository.save(orcamento);

    log.info("[ORCAMENTO_CANCELAR_SUCESSO] ID={}", salvo.getId());

    return salvo;
  }

  @Override
  public byte[] recuperarPdf(UUID id) {
    Orcamento orcamento =
        orcamentoRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado: " + id));

    OrdemServico ordemServico =
        ordemServicoRepository
            .findById(orcamento.getOrdemServicoId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Ordem de Serviço associada não encontrada: "
                            + orcamento.getOrdemServicoId()));

    Cliente cliente =
        clienteRepository
            .findById(ordemServico.getClienteId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Cliente associado não encontrado: " + ordemServico.getClienteId()));

    Veiculo veiculo =
        veiculoRepository
            .findById(ordemServico.getVeiculoId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Veículo associado não encontrado: " + ordemServico.getVeiculoId()));

    log.info("[ORCAMENTO_PDF_GERAR] Orcamento={} OS={}", orcamento.getId(), ordemServico.getId());

    return pdfService.gerarOrcamentoPdf(orcamento, ordemServico, cliente, veiculo);
  }

  private BigDecimal calcularTotal(OrdemServico os, TipoItem... tipos) {
    BigDecimal total = BigDecimal.ZERO;
    for (ItemOrdemServico item : os.getItens()) {
      boolean matches = false;
      for (TipoItem tipo : tipos) {
        if (item.getTipo() == tipo) {
          matches = true;
          break;
        }
      }
      if (matches) {
        total = total.add(item.getSubtotal());
      }
    }
    return total;
  }

  private String gerarCodigoOrcamento() {
    return "ORC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT);
  }
}
