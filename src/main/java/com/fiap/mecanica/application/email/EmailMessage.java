package com.fiap.mecanica.application.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmailMessage {

  private final List<String> to;
  private final List<String> cc;
  private final List<String> bcc;
  private final String subject;
  private final String templateName;
  private final Map<String, Object> variables;
  private final List<EmailAttachment> attachments;

  public EmailMessage(
      List<String> to,
      List<String> cc,
      List<String> bcc,
      String subject,
      String templateName,
      Map<String, Object> variables,
      List<EmailAttachment> attachments) {
    this.to = to != null ? List.copyOf(to) : List.of();
    this.cc = cc != null ? List.copyOf(cc) : List.of();
    this.bcc = bcc != null ? List.copyOf(bcc) : List.of();
    this.subject = subject;
    this.templateName = templateName;
    this.variables = variables != null ? Map.copyOf(variables) : Map.of();
    if (attachments != null) {
      this.attachments = List.copyOf(new ArrayList<>(attachments));
    } else {
      this.attachments = List.of();
    }
  }

  public List<String> getTo() {
    return Collections.unmodifiableList(to);
  }

  public List<String> getCc() {
    return Collections.unmodifiableList(cc);
  }

  public List<String> getBcc() {
    return Collections.unmodifiableList(bcc);
  }

  public String getSubject() {
    return subject;
  }

  public String getTemplateName() {
    return templateName;
  }

  public Map<String, Object> getVariables() {
    return Collections.unmodifiableMap(variables);
  }

  public List<EmailAttachment> getAttachments() {
    return Collections.unmodifiableList(attachments);
  }
}
