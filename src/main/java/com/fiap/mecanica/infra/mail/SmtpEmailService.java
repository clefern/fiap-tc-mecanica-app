package com.fiap.mecanica.infra.mail;

import com.fiap.mecanica.application.email.EmailAttachment;
import com.fiap.mecanica.application.email.EmailMessage;
import com.fiap.mecanica.application.email.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService implements EmailSender {

  private final JavaMailSender javaMailSender;
  private final TemplateEngine templateEngine;

  @Value("${mecanica.mail.fail-on-error}")
  private boolean failOnError = true;

  @Value("${mecanica.mail.from}")
  private String defaultFrom;

  @Override
  public void enviar(EmailMessage message) {
    try {
      MimeMessage mimeMessage = javaMailSender.createMimeMessage();
      boolean hasAttachments = !message.getAttachments().isEmpty();
      MimeMessageHelper helper =
          new MimeMessageHelper(mimeMessage, hasAttachments, StandardCharsets.UTF_8.name());

      if (!message.getTo().isEmpty()) {
        helper.setTo(message.getTo().toArray(new String[0]));
      }
      if (!message.getCc().isEmpty()) {
        helper.setCc(message.getCc().toArray(new String[0]));
      }
      if (!message.getBcc().isEmpty()) {
        helper.setBcc(message.getBcc().toArray(new String[0]));
      }

      if (defaultFrom != null && !defaultFrom.isBlank()) {
        helper.setFrom(defaultFrom);
      }

      helper.setSubject(message.getSubject());

      Context context = new Context();
      for (var entry : message.getVariables().entrySet()) {
        context.setVariable(entry.getKey(), entry.getValue());
      }

      String htmlBody = templateEngine.process(message.getTemplateName(), context);
      helper.setText(htmlBody, true);

      for (EmailAttachment attachment : message.getAttachments()) {
        ByteArrayResource resource = new ByteArrayResource(attachment.getContent());
        helper.addAttachment(attachment.getFilename(), resource, attachment.getContentType());
      }

      javaMailSender.send(mimeMessage);
    } catch (MessagingException | MailException | NullPointerException e) {
      log.error("❌ Falha ao enviar email", e);
      if (failOnError) {
        throw new RuntimeException("Falha no envio de email", e);
      }
    }
  }
}
