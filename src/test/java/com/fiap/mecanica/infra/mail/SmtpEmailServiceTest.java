package com.fiap.mecanica.infra.mail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.email.EmailAttachment;
import com.fiap.mecanica.application.email.EmailMessage;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

  @Mock private JavaMailSender javaMailSender;

  @Mock private TemplateEngine templateEngine;

  @InjectMocks private SmtpEmailService emailService;

  @Test
  @DisplayName("Deve enviar email genérico com sucesso")
  void deveEnviarEmailGenerico() {
    // Arrange
    MimeMessage mimeMessage = mock(MimeMessage.class);
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/template-teste"), any(Context.class)))
        .thenReturn("<html>body</html>");

    EmailMessage message =
        new EmailMessage(
            List.of("destino@test.com"),
            List.of(),
            List.of(),
            "Assunto Teste",
            "email/template-teste",
            java.util.Map.of("chave", "valor"),
            List.<EmailAttachment>of());

    // Act
    emailService.enviar(message);

    // Assert
    verify(javaMailSender).send(mimeMessage);
    verify(templateEngine).process(eq("email/template-teste"), any(Context.class));
  }

  @Test
  @DisplayName("Deve enviar email com anexos corretamente")
  void deveEnviarEmailComAnexos() {
    MimeMessage mimeMessage = mock(MimeMessage.class);
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/template-anexo"), any(Context.class)))
        .thenReturn("<html>body</html>");

    EmailAttachment attachment = new EmailAttachment("file.txt", "data".getBytes(), "text/plain");

    EmailMessage message =
        new EmailMessage(
            List.of("destino@test.com"),
            List.of(),
            List.of(),
            "Assunto Teste Anexo",
            "email/template-anexo",
            java.util.Map.of("chave", "valor"),
            List.of(attachment));

    emailService.enviar(message);

    verify(javaMailSender).send(mimeMessage);
    verify(templateEngine).process(eq("email/template-anexo"), any(Context.class));
  }

  @Test
  @DisplayName("Deve preencher TO, CC, BCC e FROM corretamente")
  void devePreencherDestinatariosECampoFrom() throws Exception {
    MimeMessage mimeMessage = new MimeMessage((Session) null);
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/template-completo"), any(Context.class)))
        .thenReturn("<html>body</html>");

    ReflectionTestUtils.setField(emailService, "defaultFrom", "no-reply@test.com");

    EmailMessage message =
        new EmailMessage(
            List.of("to1@test.com", "to2@test.com"),
            List.of("cc1@test.com"),
            List.of("bcc1@test.com", "bcc2@test.com"),
            "Assunto Completo",
            "email/template-completo",
            java.util.Map.of(),
            List.<EmailAttachment>of());

    emailService.enviar(message);

    InternetAddress[] to = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
    InternetAddress[] cc = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
    InternetAddress[] bcc =
        (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
    InternetAddress from = (InternetAddress) mimeMessage.getFrom()[0];

    org.assertj.core.api.Assertions.assertThat(to)
        .extracting(InternetAddress::getAddress)
        .containsExactlyInAnyOrder("to1@test.com", "to2@test.com");
    org.assertj.core.api.Assertions.assertThat(cc)
        .extracting(InternetAddress::getAddress)
        .containsExactly("cc1@test.com");
    org.assertj.core.api.Assertions.assertThat(bcc)
        .extracting(InternetAddress::getAddress)
        .containsExactlyInAnyOrder("bcc1@test.com", "bcc2@test.com");
    org.assertj.core.api.Assertions.assertThat(from.getAddress()).isEqualTo("no-reply@test.com");
  }

  @Test
  @DisplayName("Não deve lançar exceção quando failOnError estiver desabilitado")
  void naoDeveLancarExcecaoQuandoFailOnErrorFalse() {
    ReflectionTestUtils.setField(emailService, "failOnError", false);

    when(javaMailSender.createMimeMessage()).thenThrow(new MailSendException("Erro SMTP"));

    EmailMessage message =
        new EmailMessage(
            List.of("destino@test.com"),
            List.of(),
            List.of(),
            "Assunto Teste",
            "email/template-teste",
            java.util.Map.of(),
            List.<EmailAttachment>of());

    org.assertj.core.api.Assertions.assertThatCode(() -> emailService.enviar(message))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Deve propagar exceção ao falhar no envio")
  void devePropagarExcecaoAoFalharEnvio() {
    // Arrange
    when(javaMailSender.createMimeMessage()).thenThrow(new MailSendException("Erro SMTP"));

    EmailMessage message =
        new EmailMessage(
            List.of("destino@test.com"),
            List.of(),
            List.of(),
            "Assunto Teste",
            "email/template-teste",
            java.util.Map.of(),
            List.<EmailAttachment>of());

    // Act / Assert
    org.junit.jupiter.api.Assertions.assertThrows(
        RuntimeException.class, () -> emailService.enviar(message));
  }

  @Test
  @DisplayName("Deve ignorar campo FROM quando estiver vazio")
  void deveIgnorarFromQuandoVazio() throws Exception {
    MimeMessage mimeMessage = new MimeMessage((Session) null);
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/template-teste"), any(Context.class)))
        .thenReturn("<html>body</html>");

    ReflectionTestUtils.setField(emailService, "defaultFrom", "");

    EmailMessage message =
        new EmailMessage(
            List.of("to@test.com"),
            List.of(),
            List.of(),
            "Assunto",
            "email/template-teste",
            java.util.Map.of(),
            List.of());

    emailService.enviar(message);

    org.assertj.core.api.Assertions.assertThat(mimeMessage.getFrom()).isNull();
  }

  @Test
  @DisplayName("Deve ignorar destinatários quando lista TO estiver vazia")
  void deveIgnorarDestinatariosQuandoToVazio() throws Exception {
    MimeMessage mimeMessage = new MimeMessage((Session) null);
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(templateEngine.process(eq("email/template-teste"), any(Context.class)))
        .thenReturn("<html>body</html>");

    EmailMessage message =
        new EmailMessage(
            List.of(), // Empty TO
            List.of(),
            List.of(),
            "Assunto",
            "email/template-teste",
            java.util.Map.of(),
            List.of());

    emailService.enviar(message);

    org.assertj.core.api.Assertions.assertThat(mimeMessage.getRecipients(Message.RecipientType.TO))
        .isNull();
  }
}
