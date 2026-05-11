package com.fiap.mecanica.infra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.infra.exception.PdfGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(MockitoExtension.class)
class ThymeleafPdfServiceTest {

  @Mock private TemplateEngine templateEngine;

  private ThymeleafPdfService pdfService;

  @BeforeEach
  void setUp() {
    pdfService = new ThymeleafPdfService(templateEngine);
  }

  @Test
  @DisplayName("Deve gerar PDF com sucesso")
  void deveGerarPdfComSucesso() {
    // Arrange
    Orcamento orcamento = mock(Orcamento.class);
    OrdemServico os = mock(OrdemServico.class);
    Cliente cliente = mock(Cliente.class);
    Veiculo veiculo = mock(Veiculo.class);
    String htmlContent = "<html><body><h1>Teste</h1></body></html>";

    when(templateEngine.process(eq("pdf/orcamento-pdf"), any(Context.class)))
        .thenReturn(htmlContent);

    // Act
    byte[] pdfBytes = pdfService.gerarOrcamentoPdf(orcamento, os, cliente, veiculo);

    // Assert
    assertThat(pdfBytes).isNotEmpty();
    verify(templateEngine).process(eq("pdf/orcamento-pdf"), any(Context.class));
  }

  @Test
  @DisplayName("Deve lançar exceção quando falhar geração do HTML")
  void deveLancarExcecaoQuandoFalharHtml() {
    // Arrange
    Orcamento orcamento = mock(Orcamento.class);
    OrdemServico os = mock(OrdemServico.class);
    Cliente cliente = mock(Cliente.class);
    Veiculo veiculo = mock(Veiculo.class);

    when(templateEngine.process(eq("pdf/orcamento-pdf"), any(Context.class)))
        .thenThrow(new RuntimeException("Erro template"));

    // Act & Assert
    assertThatThrownBy(() -> pdfService.gerarOrcamentoPdf(orcamento, os, cliente, veiculo))
        .isInstanceOf(PdfGenerationException.class)
        .hasMessageContaining("Erro ao gerar PDF do orçamento");
  }

  @Test
  @DisplayName("Deve lançar exceção quando HTML for inválido para PDF")
  void deveLancarExcecaoQuandoHtmlInvalido() {
    // Arrange
    Orcamento orcamento = mock(Orcamento.class);
    OrdemServico os = mock(OrdemServico.class);
    Cliente cliente = mock(Cliente.class);
    Veiculo veiculo = mock(Veiculo.class);
    // Malformed HTML might cause ITextRenderer to fail or not, depending on
    // strictness.
    // However, ITextRenderer usually requires valid XML/XHTML.
    String invalidHtml = "<html";

    when(templateEngine.process(eq("pdf/orcamento-pdf"), any(Context.class)))
        .thenReturn(invalidHtml);

    // Act & Assert
    assertThatThrownBy(() -> pdfService.gerarOrcamentoPdf(orcamento, os, cliente, veiculo))
        .isInstanceOf(PdfGenerationException.class)
        .hasMessageContaining("Erro ao gerar PDF do orçamento");
  }
}
