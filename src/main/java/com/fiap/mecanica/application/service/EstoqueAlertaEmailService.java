package com.fiap.mecanica.application.service;

import com.fiap.mecanica.application.email.EmailAttachment;
import com.fiap.mecanica.application.email.EmailMessage;
import com.fiap.mecanica.application.email.EmailSender;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.Peca;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstoqueAlertaEmailService {

  private final EmailSender emailSender;

  @Value("${mecanica.notification.admin-email}")
  private String adminEmail;

  @Value("${mecanica.mail.admin-copy}")
  private String adminCopyEmail;

  public void enviarAlertaEstoqueBaixo(List<Peca> pecas, List<Insumo> insumos) {
    List<String> to = List.of(adminEmail);
    List<String> bcc =
        adminCopyEmail != null && !adminCopyEmail.isBlank() ? List.of(adminCopyEmail) : List.of();

    Map<String, Object> variables =
        Map.of(
            "pecas",
            pecas,
            "insumos",
            insumos,
            "dataVerificacao",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

    List<EmailAttachment> attachments = List.of();

    EmailMessage message =
        new EmailMessage(
            to,
            List.of(),
            bcc,
            "⚠️ Alerta de Estoque Baixo - Ação Necessária",
            "email/alerta-estoque-baixo",
            variables,
            attachments);

    emailSender.enviar(message);
  }
}
