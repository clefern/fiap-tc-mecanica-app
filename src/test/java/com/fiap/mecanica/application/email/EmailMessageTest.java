package com.fiap.mecanica.application.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailMessageTest {

  @Test
  @DisplayName("Deve copiar coleções e expor dados corretamente")
  void shouldCopyCollectionsAndExposeData() {
    List<String> to = new ArrayList<>(List.of("to1@example.com", "to2@example.com"));
    List<String> cc = new ArrayList<>(List.of("cc@example.com"));
    List<String> bcc = new ArrayList<>(List.of("bcc@example.com"));
    Map<String, Object> variables = new HashMap<>();
    variables.put("nome", "Cliente Teste");
    List<EmailAttachment> attachments =
        List.of(new EmailAttachment("file.txt", "x".getBytes(), "text/plain"));

    EmailMessage message =
        new EmailMessage(to, cc, bcc, "Assunto", "template", variables, attachments);

    assertThat(message.getTo()).containsExactlyElementsOf(to);
    assertThat(message.getCc()).containsExactlyElementsOf(cc);
    assertThat(message.getBcc()).containsExactlyElementsOf(bcc);
    assertThat(message.getSubject()).isEqualTo("Assunto");
    assertThat(message.getTemplateName()).isEqualTo("template");
    assertThat(message.getVariables()).containsEntry("nome", "Cliente Teste");
    assertThat(message.getAttachments())
        .hasSize(1)
        .first()
        .extracting(EmailAttachment::getFilename)
        .isEqualTo("file.txt");
  }

  @Test
  @DisplayName("Deve ser imutável em relação às coleções externas")
  void shouldBeImmutableFromExternalCollections() {
    List<String> to = new ArrayList<>(List.of("to1@example.com"));
    Map<String, Object> variables = new HashMap<>();
    variables.put("chave", "valor");
    List<EmailAttachment> attachments = new ArrayList<>();

    EmailMessage message =
        new EmailMessage(to, null, null, "Assunto", "template", variables, attachments);

    to.add("novo@example.com");
    variables.put("outra", "entrada");
    attachments.add(new EmailAttachment("file.txt", new byte[] {1}, "text/plain"));

    assertThat(message.getTo()).containsExactly("to1@example.com");
    assertThat(message.getVariables()).doesNotContainKey("outra");
    assertThat(message.getAttachments()).isEmpty();
  }

  @Test
  @DisplayName("Deve expor coleções não modificáveis")
  void shouldExposeUnmodifiableCollections() {
    EmailMessage message =
        new EmailMessage(
            List.of("to@example.com"),
            List.of("cc@example.com"),
            List.of("bcc@example.com"),
            "Assunto",
            "template",
            Map.of("k", "v"),
            List.of(new EmailAttachment("file.txt", new byte[] {1}, "text/plain")));

    assertThatThrownBy(() -> message.getTo().add("x"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> message.getCc().add("x"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> message.getBcc().add("x"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> message.getVariables().put("k2", "v2"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> message.getAttachments().add(null))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("Deve tratar nulos como coleções vazias")
  void shouldTreatNullCollectionsAsEmpty() {
    EmailMessage message = new EmailMessage(null, null, null, "Assunto", "template", null, null);

    assertThat(message.getTo()).isEmpty();
    assertThat(message.getCc()).isEmpty();
    assertThat(message.getBcc()).isEmpty();
    assertThat(message.getVariables()).isEmpty();
    assertThat(message.getAttachments()).isEmpty();
  }
}
