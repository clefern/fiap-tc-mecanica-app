package com.fiap.mecanica.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.fiap.mecanica.application.email.EmailMessage;
import com.fiap.mecanica.application.email.EmailSender;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.MecanicoRepository;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.infra.config.security.ActionTokenService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificacaoEmailApplicationServiceTest {

  @Mock private EmailSender emailSender;
  @Mock private MecanicoRepository mecanicoRepository;
  @Mock private ActionTokenService actionTokenService;

  @InjectMocks private NotificacaoEmailApplicationService service;

  @BeforeEach
  void setUp() {
    lenient().when(actionTokenService.generate(any(), any())).thenReturn("test-token");
  }

  @Test
  @DisplayName("Deve incluir dados do veículo no assunto do email de orçamento")
  void deveIncluirVeiculoNoAssuntoEmailOrcamento() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-001");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1D23"), "Corolla", "Toyota", 2020);

    byte[] pdfBytes = new byte[] {1, 2, 3};

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, pdfBytes);

    verify(emailSender).enviar(captor.capture());

    EmailMessage enviado = captor.getValue();
    assertThat(enviado.getSubject())
        .contains("ABC1D23")
        .contains("Toyota")
        .contains("Corolla")
        .contains("ORC-001");

    String linkAprovar = (String) enviado.getVariables().get("linkAprovarOrcamento");
    assertThat(linkAprovar)
        .isNotNull()
        .startsWith("http://localhost:8080/api/integracoes/orcamentos/aprovacao?token=");

    String linkRecusar = (String) enviado.getVariables().get("linkRecusarOrcamento");
    assertThat(linkRecusar)
        .isNotNull()
        .startsWith("http://localhost:8080/api/integracoes/orcamentos/aprovacao?token=");
  }

  @Test
  @DisplayName("Deve incluir adminCopyEmail no BCC quando configurado")
  void deveIncluirAdminCopyEmailNoBccQuandoConfigurado() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-BCC");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("AAA1A11"), "Modelo", "Marca", 2020);

    ReflectionTestUtils.setField(service, "adminCopyEmail", "admin-copy@test.com");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getBcc()).contains("admin-copy@test.com");
  }

  @Test
  @DisplayName("Deve gerar link de aprovação com baseUrl configurada e remover barra final")
  void deveUsarApprovalBaseUrlConfigurada() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-ABC");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("DEF1G23"), "Onix", "Chevrolet", 2021);

    ReflectionTestUtils.setField(service, "approvalBaseUrl", "https://app.mecanica.com.br/");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    String linkAprovar = (String) enviado.getVariables().get("linkAprovarOrcamento");
    assertThat(linkAprovar)
        .startsWith("https://app.mecanica.com.br/api/integracoes/orcamentos/aprovacao?token=");
  }

  @Test
  @DisplayName("Não deve incluir links de ação quando orçamento não tem ID")
  void naoDeveIncluirLinkQuandoOrcamentoIdNulo() {
    Orcamento orcamento = new Orcamento();
    orcamento.setCodigo("ORC-SEM-ID");

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("HIJ1K23"), "Gol", "Volkswagen", 2015);

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getVariables()).doesNotContainKey("linkAprovarOrcamento");
    assertThat(enviado.getVariables()).doesNotContainKey("linkRecusarOrcamento");
  }

  @Test
  @DisplayName("Deve incluir nome do mecânico de diagnóstico quando presente")
  void deveIncluirNomeMecanicoDiagnosticoQuandoPresente() {
    UUID mecanicoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-MEC");
    orcamento.setOrdemServicoId(UUID.randomUUID());
    orcamento.setMecanicoDiagnosticoId(mecanicoId);

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("LMN1O23"), "Cronos", "Fiat", 2022);

    Mecanico mecanico = new Mecanico();
    mecanico.setNome("Mecânico Diagnóstico");

    org.mockito.Mockito.when(mecanicoRepository.findById(mecanicoId))
        .thenReturn(Optional.of(mecanico));

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getVariables())
        .containsEntry("nomeMecanicoDiagnostico", "Mecânico Diagnóstico");
  }

  @Test
  @DisplayName("Deve incluir dados do veículo no assunto do email de OS criada")
  void deveIncluirVeiculoNoAssuntoEmailOsCriada() {
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-123");

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("XYZ1A23"), "Fiesta", "Ford", 2018);

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarConfirmacaoAbertura(os, cliente, veiculo);

    verify(emailSender).enviar(captor.capture());

    EmailMessage enviado = captor.getValue();
    assertThat(enviado.getSubject())
        .contains("XYZ1A23")
        .contains("Ford")
        .contains("Fiesta")
        .contains("OS-123");
  }

  @Test
  @DisplayName("Deve incluir dados do veículo no assunto do email de OS finalizada")
  void deveIncluirVeiculoNoAssuntoEmailOsFinalizada() {
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-999");

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("XYZ9C99"), "Civic", "Honda", 2019);

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarAvisoConclusao(os, cliente, veiculo);

    verify(emailSender).enviar(captor.capture());

    EmailMessage enviado = captor.getValue();
    assertThat(enviado.getSubject())
        .contains("XYZ9C99")
        .contains("Honda")
        .contains("Civic")
        .contains("OS-999");
  }

  @Test
  @DisplayName("Deve ignorar nome do mecânico quando não encontrado no repositório")
  void deveIgnorarMecanicoNaoEncontrado() {
    UUID mecanicoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-MEC-404");
    orcamento.setOrdemServicoId(UUID.randomUUID());
    orcamento.setMecanicoDiagnosticoId(mecanicoId);

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    org.mockito.Mockito.when(mecanicoRepository.findById(mecanicoId)).thenReturn(Optional.empty());

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getVariables()).doesNotContainKey("nomeMecanicoDiagnostico");
  }

  @Test
  @DisplayName("Deve ignorar anexo quando PDF está vazio")
  void deveIgnorarPdfVazio() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-PDF-VAZIO");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    byte[] pdfBytes = new byte[0];

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, pdfBytes);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getAttachments()).isEmpty();
  }

  @Test
  @DisplayName("Deve usar baseUrl padrão quando configuração estiver vazia")
  void deveUsarBaseUrlPadraoQuandoConfiguracaoVazia() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-BASE-VAZIA");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ReflectionTestUtils.setField(service, "approvalBaseUrl", "");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    String linkAprovar = (String) enviado.getVariables().get("linkAprovarOrcamento");
    assertThat(linkAprovar).startsWith("http://localhost:8080");
  }

  @Test
  @DisplayName("Deve usar baseUrl padrão quando configuração for nula")
  void deveUsarBaseUrlPadraoQuandoConfiguracaoNula() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-BASE-NULA");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ReflectionTestUtils.setField(service, "approvalBaseUrl", null);

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    String linkAprovar = (String) enviado.getVariables().get("linkAprovarOrcamento");
    assertThat(linkAprovar).startsWith("http://localhost:8080");
  }

  @Test
  @DisplayName("Deve remover barra final da baseUrl configurada")
  void deveRemoverBarraFinalBaseUrl() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-SLASH");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ReflectionTestUtils.setField(service, "approvalBaseUrl", "http://my-url.com/");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    String linkAprovar = (String) enviado.getVariables().get("linkAprovarOrcamento");
    assertThat(linkAprovar)
        .startsWith("http://my-url.com/api/integracoes/orcamentos/aprovacao?token=");
  }

  @Test
  @DisplayName("Deve ignorar anexo quando PDF é nulo")
  void deveIgnorarPdfNulo() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-PDF-NULL");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getAttachments()).isEmpty();
  }

  @Test
  @DisplayName("Deve incluir BCC em confirmação de abertura quando configurado")
  void deveIncluirBccEmConfirmacaoAbertura() {
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-BCC-OPEN");

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ReflectionTestUtils.setField(service, "adminCopyEmail", "admin-copy@test.com");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarConfirmacaoAbertura(os, cliente, veiculo);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getBcc()).contains("admin-copy@test.com");
  }

  @Test
  @DisplayName("Deve incluir BCC em aviso de conclusão quando configurado")
  void deveIncluirBccEmAvisoConclusao() {
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-BCC-CLOSE");

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ReflectionTestUtils.setField(service, "adminCopyEmail", "admin-copy@test.com");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarAvisoConclusao(os, cliente, veiculo);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getBcc()).contains("admin-copy@test.com");
  }

  @Test
  @DisplayName("Não deve incluir BCC em confirmação de abertura quando configuração vazia")
  void naoDeveIncluirBccEmConfirmacaoAberturaQuandoConfiguracaoVazia() {
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-NO-BCC-OPEN");

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ReflectionTestUtils.setField(service, "adminCopyEmail", "");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarConfirmacaoAbertura(os, cliente, veiculo);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getBcc()).isEmpty();
  }

  @Test
  @DisplayName("Não deve incluir BCC em aviso de conclusão quando configuração vazia")
  void naoDeveIncluirBccEmAvisoConclusaoQuandoConfiguracaoVazia() {
    OrdemServico os = new OrdemServico();
    os.setCodigo("OS-NO-BCC-CLOSE");

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ReflectionTestUtils.setField(service, "adminCopyEmail", "");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarAvisoConclusao(os, cliente, veiculo);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getBcc()).isEmpty();
  }

  @Test
  @DisplayName("Não deve incluir BCC em orçamento quando configuração vazia")
  void naoDeveIncluirBccEmOrcamentoQuandoConfiguracaoVazia() {
    Orcamento orcamento = new Orcamento();
    orcamento.setId(UUID.randomUUID());
    orcamento.setCodigo("ORC-NO-BCC");
    orcamento.setOrdemServicoId(UUID.randomUUID());

    Cliente cliente = new Cliente();
    cliente.setEmail(Email.of("cliente@test.com"));

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Test", "Test", 2020);

    ReflectionTestUtils.setField(service, "adminCopyEmail", "");

    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);

    service.enviarOrcamento(orcamento, cliente, veiculo, null);

    verify(emailSender).enviar(captor.capture());
    EmailMessage enviado = captor.getValue();

    assertThat(enviado.getBcc()).isEmpty();
  }
}
