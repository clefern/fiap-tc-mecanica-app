package com.fiap.mecanica.infra.service;

import com.fiap.mecanica.application.service.PdfService;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.infra.exception.PdfGenerationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
@RequiredArgsConstructor
public class ThymeleafPdfService implements PdfService {

  private final TemplateEngine templateEngine;

  @Override
  public byte[] gerarOrcamentoPdf(
      Orcamento orcamento, OrdemServico ordemServico, Cliente cliente, Veiculo veiculo) {
    Context context = new Context();
    context.setVariable("orcamento", orcamento);
    context.setVariable("os", ordemServico);
    context.setVariable("cliente", cliente);
    context.setVariable("veiculo", veiculo);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      String html = templateEngine.process("pdf/orcamento-pdf", context);

      ITextRenderer renderer = new ITextRenderer();
      renderer.setDocumentFromString(html);
      renderer.layout();
      renderer.createPDF(outputStream);
      return outputStream.toByteArray();
    } catch (IOException | RuntimeException e) {
      throw new PdfGenerationException("Erro ao gerar PDF do orçamento", e);
    }
  }
}
