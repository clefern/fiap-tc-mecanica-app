package com.fiap.mecanica.application.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.events.OrcamentoGeradoEvent;
import com.fiap.mecanica.application.events.OsCriadaEvent;
import com.fiap.mecanica.application.service.NotificacaoEmailApplicationService;
import com.fiap.mecanica.application.service.PdfService;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificacaoEmailListenerTest {

  @Mock private NotificacaoEmailApplicationService notificacaoEmailApplicationService;
  @Mock private PdfService pdfService;
  @Mock private OrdemServicoRepository ordemServicoRepository;
  @Mock private ClienteRepository clienteRepository;
  @Mock private VeiculoRepository veiculoRepository;

  @InjectMocks private NotificacaoEmailListener listener;

  @Test
  @DisplayName("Deve enviar email de orçamento com sucesso")
  void deveEnviarEmailOrcamentoComSucesso() {
    // Arrange
    UUID osId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setCodigo("ORC-123");
    orcamento.setOrdemServicoId(osId);

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setClienteId(clienteId);
    os.setVeiculoId(veiculoId);
    os.setItens(Collections.singletonList(new ItemOrdemServico()));

    Cliente cliente = new Cliente();
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Modelo Teste", "Marca Teste", 2020);

    byte[] pdfBytes = new byte[] {1, 2, 3};

    when(ordemServicoRepository.findById(osId)).thenReturn(Optional.of(os));
    when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
    when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));
    when(pdfService.gerarOrcamentoPdf(orcamento, os, cliente, veiculo)).thenReturn(pdfBytes);

    OrcamentoGeradoEvent event = new OrcamentoGeradoEvent(this, orcamento);

    // Act
    listener.onOrcamentoGerado(event);

    // Assert
    verify(notificacaoEmailApplicationService)
        .enviarOrcamento(orcamento, cliente, veiculo, pdfBytes);
  }

  @Test
  @DisplayName("Não deve enviar email de orçamento se OS não for encontrada")
  void naoDeveEnviarEmailOrcamentoSeOsNaoEncontrada() {
    // Arrange
    UUID osId = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    orcamento.setCodigo("ORC-123");
    orcamento.setOrdemServicoId(osId);

    when(ordemServicoRepository.findById(osId)).thenReturn(Optional.empty());

    OrcamentoGeradoEvent event = new OrcamentoGeradoEvent(this, orcamento);

    // Act
    listener.onOrcamentoGerado(event);

    // Assert
    verify(notificacaoEmailApplicationService, never()).enviarOrcamento(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Deve lidar com exceção ao gerar PDF")
  void deveLidarComExcecaoAoGerarPdf() {
    // Arrange
    UUID osId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setCodigo("ORC-123");
    orcamento.setOrdemServicoId(osId);

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setClienteId(clienteId);
    os.setVeiculoId(veiculoId);

    Cliente cliente = new Cliente();
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Modelo Teste", "Marca Teste", 2020);

    when(ordemServicoRepository.findById(osId)).thenReturn(Optional.of(os));
    when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
    when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));
    doThrow(new RuntimeException("Erro PDF"))
        .when(pdfService)
        .gerarOrcamentoPdf(orcamento, os, cliente, veiculo);

    OrcamentoGeradoEvent event = new OrcamentoGeradoEvent(this, orcamento);

    // Act
    listener.onOrcamentoGerado(event);

    // Assert
    verify(notificacaoEmailApplicationService, never()).enviarOrcamento(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Deve enviar email de abertura de OS com sucesso")
  void deveEnviarEmailAberturaOsComSucesso() {
    // Arrange
    UUID osId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setCodigo("OS-123");
    os.setClienteId(clienteId);
    os.setVeiculoId(veiculoId);

    Cliente cliente = new Cliente();
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Modelo Teste", "Marca Teste", 2020);

    when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
    when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));

    OsCriadaEvent event = new OsCriadaEvent(this, os);

    // Act
    listener.onOsCriada(event);

    // Assert
    verify(notificacaoEmailApplicationService).enviarConfirmacaoAbertura(os, cliente, veiculo);
  }

  @Test
  @DisplayName("Não deve enviar email de abertura de OS se cliente não for encontrado")
  void naoDeveEnviarEmailAberturaOsSeClienteNaoEncontrado() {
    // Arrange
    UUID clienteId = UUID.randomUUID();
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-123");
    os.setClienteId(clienteId);

    when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

    OsCriadaEvent event = new OsCriadaEvent(this, os);

    // Act
    listener.onOsCriada(event);

    // Assert
    verify(notificacaoEmailApplicationService, never())
        .enviarConfirmacaoAbertura(any(), any(), any());
  }
}
