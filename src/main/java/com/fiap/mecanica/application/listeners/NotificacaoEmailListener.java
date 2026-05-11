package com.fiap.mecanica.application.listeners;

import com.fiap.mecanica.application.events.OrcamentoGeradoEvent;
import com.fiap.mecanica.application.events.OsCriadaEvent;
import com.fiap.mecanica.application.events.OsFinalizadaEvent;
import com.fiap.mecanica.application.service.NotificacaoEmailApplicationService;
import com.fiap.mecanica.application.service.PdfService;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoEmailListener {

  private final NotificacaoEmailApplicationService notificacaoEmailApplicationService;
  private final PdfService pdfService;
  private final OrdemServicoRepository ordemServicoRepository;
  private final ClienteRepository clienteRepository;
  private final VeiculoRepository veiculoRepository;

  @Async
  @EventListener
  @Transactional(readOnly = true)
  public void onOrcamentoGerado(OrcamentoGeradoEvent event) {
    Orcamento orcamento = event.getOrcamento();
    log.info("Evento recebido: Enviando email para orçamento {}", orcamento.getCodigo());

    try {
      OrdemServico ordemServico =
          ordemServicoRepository
              .findById(orcamento.getOrdemServicoId())
              .orElseThrow(() -> new RuntimeException("OS não encontrada para envio de email"));

      // Força a inicialização da lista de itens para evitar
      // LazyInitializationException
      // se o mapeamento estiver acessando essa coleção
      if (ordemServico.getItens() != null) {
        log.debug("Inicializando itens para email. Quantidade: {}", ordemServico.getItens().size());
      }

      Cliente cliente =
          clienteRepository
              .findById(ordemServico.getClienteId())
              .orElseThrow(
                  () -> new RuntimeException("Cliente não encontrado para envio de email"));

      Veiculo veiculo =
          veiculoRepository
              .findById(ordemServico.getVeiculoId())
              .orElseThrow(
                  () -> new RuntimeException("Veículo não encontrado para envio de email"));

      byte[] pdfBytes = pdfService.gerarOrcamentoPdf(orcamento, ordemServico, cliente, veiculo);
      log.info("PDF gerado com sucesso. Tamanho: {} bytes", pdfBytes != null ? pdfBytes.length : 0);

      notificacaoEmailApplicationService.enviarOrcamento(orcamento, cliente, veiculo, pdfBytes);

    } catch (Exception e) {
      log.error("Falha ao processar envio de email para orçamento {}", orcamento.getCodigo(), e);
    }
  }

  @Async
  @EventListener
  @Transactional(readOnly = true)
  public void onOsCriada(OsCriadaEvent event) {
    OrdemServico os = event.getOrdemServico();
    log.info("Evento recebido: Enviando email de abertura para OS {}", os.getCodigo());

    try {
      Cliente cliente =
          clienteRepository
              .findById(os.getClienteId())
              .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

      Veiculo veiculo =
          veiculoRepository
              .findById(os.getVeiculoId())
              .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

      notificacaoEmailApplicationService.enviarConfirmacaoAbertura(os, cliente, veiculo);

    } catch (Exception e) {
      log.error("Falha ao processar envio de email de abertura para OS {}", os.getCodigo(), e);
    }
  }

  @Async
  @EventListener
  @Transactional(readOnly = true)
  public void onOsFinalizada(OsFinalizadaEvent event) {
    OrdemServico os = event.getOrdemServico();
    log.info("Evento recebido: Enviando email de conclusão para OS {}", os.getCodigo());

    try {
      Cliente cliente =
          clienteRepository
              .findById(os.getClienteId())
              .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

      Veiculo veiculo =
          veiculoRepository
              .findById(os.getVeiculoId())
              .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

      notificacaoEmailApplicationService.enviarAvisoConclusao(os, cliente, veiculo);

    } catch (Exception e) {
      log.error("Falha ao processar envio de email de conclusão para OS {}", os.getCodigo(), e);
    }
  }
}
