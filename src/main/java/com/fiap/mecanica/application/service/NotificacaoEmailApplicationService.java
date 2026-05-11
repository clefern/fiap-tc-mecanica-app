package com.fiap.mecanica.application.service;

import com.fiap.mecanica.application.email.EmailAttachment;
import com.fiap.mecanica.application.email.EmailMessage;
import com.fiap.mecanica.application.email.EmailSender;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.MecanicoRepository;
import com.fiap.mecanica.infra.config.security.ActionTokenService;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import com.fiap.mecanica.presentation.dto.DecisaoOrcamento;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacaoEmailApplicationService {

  private final EmailSender emailSender;
  private final MecanicoRepository mecanicoRepository;
  private final ActionTokenService actionTokenService;

  @Value("${mecanica.mail.admin-copy}")
  private String adminCopyEmail;

  @Value("${mecanica.mail.approval-base-url:}")
  private String approvalBaseUrl;

  @MonitoredOperation("email.enviarOrcamento")
  public void enviarOrcamento(
      Orcamento orcamento, Cliente cliente, Veiculo veiculo, byte[] pdfBytes) {
    List<String> to = List.of(cliente.getEmail().value());
    List<String> bcc =
        adminCopyEmail != null && !adminCopyEmail.isBlank() ? List.of(adminCopyEmail) : List.of();

    Map<String, Object> variables = new HashMap<>();
    variables.put("orcamento", orcamento);
    variables.put("cliente", cliente);
    variables.put("veiculo", veiculo);

    String baseUrl =
        approvalBaseUrl == null || approvalBaseUrl.isBlank()
            ? "http://localhost:8080"
            : approvalBaseUrl;
    String normalizedBaseUrl =
        baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    if (orcamento.getId() != null) {
      String approveToken =
          actionTokenService.generate(orcamento.getId(), DecisaoOrcamento.APROVADO);
      String rejectToken =
          actionTokenService.generate(orcamento.getId(), DecisaoOrcamento.REPROVADO);
      String actionBase = normalizedBaseUrl + "/api/integracoes/orcamentos/aprovacao?token=";
      variables.put("linkAprovarOrcamento", actionBase + approveToken);
      variables.put("linkRecusarOrcamento", actionBase + rejectToken);
    }

    if (orcamento.getMecanicoDiagnosticoId() != null) {
      mecanicoRepository
          .findById(orcamento.getMecanicoDiagnosticoId())
          .ifPresent(mecanico -> variables.put("nomeMecanicoDiagnostico", mecanico.getNome()));
    }

    EmailAttachment attachment = null;
    if (pdfBytes != null && pdfBytes.length > 0) {
      String pdfName = "orcamento-" + orcamento.getCodigo() + ".pdf";
      attachment = new EmailAttachment(pdfName, pdfBytes, "application/pdf");
    }

    List<EmailAttachment> attachments = attachment != null ? List.of(attachment) : List.of();

    String veiculoDescricao =
        veiculo.getPlaca().value() + " (" + veiculo.getMarca() + " " + veiculo.getModelo() + ")";

    EmailMessage message =
        new EmailMessage(
            to,
            List.of(),
            bcc,
            "Seu orçamento do veículo "
                + veiculoDescricao
                + " está pronto: "
                + orcamento.getCodigo(),
            "email/orcamento-gerado",
            variables,
            attachments);

    emailSender.enviar(message);
  }

  @MonitoredOperation("email.enviarConfirmacaoOs")
  public void enviarConfirmacaoAbertura(OrdemServico os, Cliente cliente, Veiculo veiculo) {
    List<String> to = List.of(cliente.getEmail().value());
    List<String> bcc =
        adminCopyEmail != null && !adminCopyEmail.isBlank() ? List.of(adminCopyEmail) : List.of();

    Map<String, Object> variables = Map.of("os", os, "cliente", cliente, "veiculo", veiculo);

    List<EmailAttachment> attachments = List.of();

    String veiculoDescricao =
        veiculo.getPlaca().value() + " (" + veiculo.getMarca() + " " + veiculo.getModelo() + ")";

    EmailMessage message =
        new EmailMessage(
            to,
            List.of(),
            bcc,
            "OS aberta para veículo " + veiculoDescricao + " - OS " + os.getCodigo(),
            "email/os-criada",
            variables,
            attachments);

    emailSender.enviar(message);
  }

  public void enviarAvisoConclusao(OrdemServico os, Cliente cliente, Veiculo veiculo) {
    List<String> to = List.of(cliente.getEmail().value());
    List<String> bcc =
        adminCopyEmail != null && !adminCopyEmail.isBlank() ? List.of(adminCopyEmail) : List.of();

    Map<String, Object> variables = Map.of("os", os, "cliente", cliente, "veiculo", veiculo);

    List<EmailAttachment> attachments = List.of();

    String veiculoDescricao =
        veiculo.getPlaca().value() + " (" + veiculo.getMarca() + " " + veiculo.getModelo() + ")";

    EmailMessage message =
        new EmailMessage(
            to,
            List.of(),
            bcc,
            "Seu veículo " + veiculoDescricao + " está pronto! OS: " + os.getCodigo(),
            "email/os-finalizada",
            variables,
            attachments);

    emailSender.enviar(message);
  }
}
