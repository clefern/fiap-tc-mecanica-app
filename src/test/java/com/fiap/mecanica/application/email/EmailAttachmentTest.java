package com.fiap.mecanica.application.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailAttachmentTest {

  @Test
  @DisplayName("Deve expor os campos corretamente")
  void shouldExposeFieldsCorrectly() {
    String filename = "report.pdf";
    byte[] content = "data".getBytes();
    String contentType = "application/pdf";

    EmailAttachment attachment = new EmailAttachment(filename, content, contentType);

    assertThat(attachment.getFilename()).isEqualTo(filename);
    assertThat(attachment.getContent()).isEqualTo(content);
    assertThat(attachment.getContentType()).isEqualTo(contentType);
  }

  @Test
  @DisplayName("Deve permitir campos nulos")
  void shouldAllowNullFields() {
    EmailAttachment attachment = new EmailAttachment(null, null, null);

    assertThat(attachment.getFilename()).isNull();
    assertThat(attachment.getContent()).isNull();
    assertThat(attachment.getContentType()).isNull();
  }
}
