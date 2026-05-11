package com.fiap.mecanica.application.email;

public class EmailAttachment {

  private final String filename;
  private final byte[] content;
  private final String contentType;

  public EmailAttachment(String filename, byte[] content, String contentType) {
    this.filename = filename;
    this.content = content;
    this.contentType = contentType;
  }

  public String getFilename() {
    return filename;
  }

  public byte[] getContent() {
    return content;
  }

  public String getContentType() {
    return contentType;
  }
}
