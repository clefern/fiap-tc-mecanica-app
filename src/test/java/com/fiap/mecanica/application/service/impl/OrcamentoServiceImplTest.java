package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.events.OrcamentoAprovadoEvent;
import com.fiap.mecanica.application.events.OrcamentoGeradoEvent;
import com.fiap.mecanica.application.events.OrcamentoReprovadoEvent;
import com.fiap.mecanica.application.service.PdfService;
import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.OrcamentoRepository;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.infrastructure.integration.estoque.EstoqueGateway;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceImplTest {

  @Mock private OrcamentoRepository orcamentoRepository;
  @Mock private OrdemServicoRepository ordemServicoRepository;
  @Mock private ClienteRepository clienteRepository;
  @Mock private VeiculoRepository veiculoRepository;
  @Mock private PdfService pdfService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private EstoqueGateway estoqueGateway;

  private OrcamentoServiceImpl orcamentoService;

  @BeforeEach
  void setUp() {
    orcamentoService =
        new OrcamentoServiceImpl(
            orcamentoRepository,
            ordemServicoRepository,
            clienteRepository,
            veiculoRepository,
            pdfService,
            eventPublisher,
            estoqueGateway,
            new BigDecimal("0.05"));
  }

  @Test
  @DisplayName("Deve gerar orçamento com sucesso calculando valores corretamente")
  void deveGerarOrcamentoComSucesso() {
    // Arrange
    UUID osId = UUID.randomUUID();
    ItemOrdemServico peca =
        ItemOrdemServico.builder()
            .tipo(TipoItem.PECA)
            .valorUnitario(new BigDecimal("100.00"))
            .quantidade(2)
            .build(); // Total 200.00

    ItemOrdemServico servico =
        ItemOrdemServico.builder()
            .tipo(TipoItem.SERVICO)
            .valorUnitario(new BigDecimal("150.00"))
            .quantidade(1)
            .build(); // Total 150.00

    OrdemServico ordemServico =
        OrdemServico.builder().id(osId).itens(List.of(peca, servico)).build();

    when(orcamentoRepository.save(any(Orcamento.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(orcamentoRepository.existsByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(false);

    // Act
    Orcamento orcamento = orcamentoService.gerarOrcamento(ordemServico);

    // Assert
    assertThat(orcamento).isNotNull();
    assertThat(orcamento.getOrdemServicoId()).isEqualTo(osId);
    assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.GERADO);

    // Verificação de valores
    // Materiais: 2 * 100 = 200
    assertThat(orcamento.getValorTotalMateriais()).isEqualByComparingTo(new BigDecimal("200.00"));

    // Mão de obra: 1 * 150 = 150
    assertThat(orcamento.getValorTotalMaoDeObra()).isEqualByComparingTo(new BigDecimal("150.00"));

    // Impostos: 5% de 150 (mão de obra) = 7.50
    assertThat(orcamento.getValorImpostos()).isEqualByComparingTo(new BigDecimal("7.50"));

    // Total: 200 + 150 + 7.50 = 357.50
    assertThat(orcamento.getValorTotal()).isEqualByComparingTo(new BigDecimal("357.50"));

    verify(orcamentoRepository, times(2))
        .save(any(Orcamento.class)); // Salva inicial e salva com URL
    verify(eventPublisher).publishEvent(any(OrcamentoGeradoEvent.class));
  }

  @Test
  @DisplayName("Deve cancelar orçamento GERADO existente e gerar um novo")
  void deveCancelarOrcamentoGeradoExistenteEGerarNovo() {
    UUID osId = UUID.randomUUID();
    OrdemServico ordemServico = OrdemServico.builder().id(osId).itens(List.of()).build();

    Orcamento existente =
        Orcamento.builder()
            .id(UUID.randomUUID())
            .ordemServicoId(osId)
            .status(StatusOrcamento.GERADO)
            .build();

    when(orcamentoRepository.existsByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(true);
    when(orcamentoRepository.findByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(Optional.of(existente));
    when(orcamentoRepository.save(any(Orcamento.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Orcamento novo = orcamentoService.gerarOrcamento(ordemServico);

    assertThat(existente.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);
    assertThat(novo.getId()).isNotEqualTo(existente.getId());
    verify(orcamentoRepository).findByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO);
    verify(orcamentoRepository, times(3)).save(any(Orcamento.class));
  }

  @Test
  @DisplayName("Deve listar todos os orçamentos paginados")
  void deveListarTodosPaginados() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<Orcamento> page = new PageImpl<>(List.of(new Orcamento()));
    when(orcamentoRepository.findAll(pageable)).thenReturn(page);

    // Act
    Page<Orcamento> result = orcamentoService.listarTodos(pageable);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(orcamentoRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Deve buscar orçamento por ID")
  void deveBuscarPorId() {
    // Arrange
    UUID id = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    orcamento.setId(id);
    when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));

    // Act
    Optional<Orcamento> result = orcamentoService.buscarPorId(id);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(id);
  }

  @Test
  @DisplayName("Deve deletar orçamento por ID")
  void deveDeletarPorId() {
    // Arrange
    UUID id = UUID.randomUUID();

    // Act
    orcamentoService.deletar(id);

    // Assert
    verify(orcamentoRepository).deleteById(id);
  }

  @Test
  @DisplayName("Deve recuperar PDF do orçamento com sucesso")
  void deveRecuperarPdf() {
    // Arrange
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setClienteId(clienteId);
    os.setVeiculoId(veiculoId);

    Cliente cliente = new Cliente();
    cliente.setId(clienteId);

    Veiculo veiculo = mock(Veiculo.class);

    byte[] expectedPdf = new byte[] {1, 2, 3};

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findById(osId)).thenReturn(Optional.of(os));
    when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
    when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));
    when(pdfService.gerarOrcamentoPdf(orcamento, os, cliente, veiculo)).thenReturn(expectedPdf);

    // Act
    byte[] result = orcamentoService.recuperarPdf(orcamentoId);

    // Assert
    assertThat(result).isEqualTo(expectedPdf);
    verify(pdfService).gerarOrcamentoPdf(orcamento, os, cliente, veiculo);
  }

  @Test
  @DisplayName("Deve aprovar orçamento e baixar estoque via cliente externo")
  void deveAprovarOrcamentoBaixandoEstoque() {
    // Arrange
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID pecaId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);
    orcamento.setStatus(StatusOrcamento.GERADO);

    ItemOrdemServico itemPeca =
        ItemOrdemServico.builder().referenciaId(pecaId).tipo(TipoItem.PECA).quantidade(5).build();

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setItens(List.of(itemPeca));

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findByIdWithItens(osId)).thenReturn(Optional.of(os));
    when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(i -> i.getArgument(0));

    // Act
    Orcamento aprovado = orcamentoService.aprovar(orcamentoId);

    // Assert
    assertThat(aprovado.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    verify(estoqueGateway).baixarEstoque(pecaId, TipoItem.PECA, 5);
    verify(orcamentoRepository).save(orcamento);
    verify(eventPublisher).publishEvent(any(OrcamentoAprovadoEvent.class));
  }

  @Test
  @DisplayName("Deve reprovar orçamento e publicar evento")
  void deveReprovarOrcamentoPublicandoEvento() {
    UUID orcamentoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setStatus(StatusOrcamento.GERADO);

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(i -> i.getArgument(0));

    Orcamento reprovado = orcamentoService.reprovar(orcamentoId);

    assertThat(reprovado.getStatus()).isEqualTo(StatusOrcamento.REJEITADO);
    verify(orcamentoRepository).save(orcamento);
    verify(eventPublisher).publishEvent(any(OrcamentoReprovadoEvent.class));
  }

  // --- Novos Testes de Cobertura ---

  @Test
  @DisplayName("Deve lançar exceção ao tentar aprovar orçamento inexistente")
  void deveLancarExcecaoAoAprovarOrcamentoInexistente() {
    UUID id = UUID.randomUUID();
    when(orcamentoRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orcamentoService.aprovar(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Orçamento não encontrado");
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar aprovar orçamento com OS inexistente")
  void deveLancarExcecaoAoAprovarComOsInexistente() {
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findByIdWithItens(osId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orcamentoService.aprovar(orcamentoId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("OS não encontrada");
  }

  @Test
  @DisplayName("Deve aprovar orçamento por ID da OS")
  void deveAprovarOrcamentoPorOsId() {
    // Arrange
    UUID osId = UUID.randomUUID();
    UUID orcamentoId = UUID.randomUUID();
    UUID pecaId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);
    orcamento.setStatus(StatusOrcamento.GERADO);

    ItemOrdemServico itemPeca =
        ItemOrdemServico.builder().referenciaId(pecaId).tipo(TipoItem.PECA).quantidade(5).build();

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setItens(List.of(itemPeca));

    when(orcamentoRepository.findByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(Optional.of(orcamento));
    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findByIdWithItens(osId)).thenReturn(Optional.of(os));
    when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(i -> i.getArgument(0));

    // Act
    Orcamento aprovado = orcamentoService.aprovarPorOsId(osId);

    // Assert
    assertThat(aprovado.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    verify(estoqueGateway).baixarEstoque(pecaId, TipoItem.PECA, 5);
    verify(orcamentoRepository).save(orcamento);
    verify(eventPublisher).publishEvent(any(OrcamentoAprovadoEvent.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar aprovar por OS sem orçamento GERADO")
  void deveLancarExcecaoAoAprovarPorOsIdSemOrcamentoGerado() {
    UUID osId = UUID.randomUUID();
    when(orcamentoRepository.findByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(Optional.empty());
    // Fallback to findAllByOrdemServicoId if searching by latest, but seeking "GERADO" specifically
    when(orcamentoRepository.findAllByOrdemServicoId(osId)).thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> orcamentoService.aprovarPorOsId(osId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Nenhum orçamento em estado GERADO encontrado para a OS");
  }

  @Test
  @DisplayName("Deve reprovar orçamento por ID da OS")
  void deveReprovarOrcamentoPorOsId() {
    // Arrange
    UUID osId = UUID.randomUUID();
    UUID orcamentoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);
    orcamento.setStatus(StatusOrcamento.GERADO);

    when(orcamentoRepository.findByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(Optional.of(orcamento));
    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(i -> i.getArgument(0));

    // Act
    Orcamento reprovado = orcamentoService.reprovarPorOsId(osId);

    // Assert
    assertThat(reprovado.getStatus()).isEqualTo(StatusOrcamento.REJEITADO);
    verify(orcamentoRepository).save(orcamento);
    verify(eventPublisher).publishEvent(any(OrcamentoReprovadoEvent.class));
  }

  @Test
  @DisplayName("Deve propagar erro ao falhar baixa de estoque na aprovação")
  void devePropagarErroAoFalharBaixaEstoque() {
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID pecaId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);
    orcamento.setStatus(StatusOrcamento.GERADO);

    ItemOrdemServico itemPeca =
        ItemOrdemServico.builder().referenciaId(pecaId).tipo(TipoItem.PECA).quantidade(5).build();

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setItens(List.of(itemPeca));

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findByIdWithItens(osId)).thenReturn(Optional.of(os));

    doThrow(new RuntimeException("Erro estoque"))
        .when(estoqueGateway)
        .baixarEstoque(pecaId, TipoItem.PECA, 5);

    assertThatThrownBy(() -> orcamentoService.aprovar(orcamentoId))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Erro estoque");

    // Verifica que não salvou como aprovado
    verify(orcamentoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar reprovar orçamento inexistente")
  void deveLancarExcecaoAoReprovarOrcamentoInexistente() {
    UUID id = UUID.randomUUID();
    when(orcamentoRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orcamentoService.reprovar(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Orçamento não encontrado");
  }

  @Test
  @DisplayName("Deve buscar orçamento ativo por OS")
  void deveBuscarPorOrdemServicoAtivo() {
    UUID osId = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    orcamento.setStatus(StatusOrcamento.GERADO);

    when(orcamentoRepository.findByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(Optional.of(orcamento));

    Optional<Orcamento> result = orcamentoService.buscarPorOrdemServico(osId);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(orcamento);
  }

  @Test
  @DisplayName("Deve buscar orçamento mais recente do histórico quando não houver ativo")
  void deveBuscarPorOrdemServicoHistorico() {
    UUID osId = UUID.randomUUID();

    Orcamento antigo = new Orcamento();
    antigo.setDataEmissao(LocalDateTime.now().minusDays(2));
    antigo.setStatus(StatusOrcamento.CANCELADO);

    Orcamento recente = new Orcamento();
    recente.setDataEmissao(LocalDateTime.now().minusDays(1));
    recente.setStatus(StatusOrcamento.REJEITADO);

    when(orcamentoRepository.findByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(Optional.empty());
    when(orcamentoRepository.findAllByOrdemServicoId(osId)).thenReturn(List.of(antigo, recente));

    Optional<Orcamento> result = orcamentoService.buscarPorOrdemServico(osId);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(recente);
  }

  @Test
  @DisplayName("Deve retornar vazio ao buscar por OS sem nenhum orçamento")
  void deveRetornarVazioBuscarPorOrdemServicoSemResultados() {
    UUID osId = UUID.randomUUID();

    when(orcamentoRepository.findByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(Optional.empty());
    when(orcamentoRepository.findAllByOrdemServicoId(osId)).thenReturn(Collections.emptyList());

    Optional<Orcamento> result = orcamentoService.buscarPorOrdemServico(osId);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve cancelar orçamentos pendentes")
  void deveCancelarOrcamentosPendentes() {
    UUID osId = UUID.randomUUID();

    Orcamento pendente = new Orcamento();
    pendente.setStatus(StatusOrcamento.GERADO);

    Orcamento jaFinalizado = new Orcamento();
    jaFinalizado.setStatus(StatusOrcamento.APROVADO);

    when(orcamentoRepository.findAllByOrdemServicoId(osId))
        .thenReturn(List.of(pendente, jaFinalizado));

    orcamentoService.cancelarOrcamentosPendentes(osId);

    assertThat(pendente.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);
    assertThat(jaFinalizado.getStatus()).isEqualTo(StatusOrcamento.APROVADO); // Inalterado

    verify(orcamentoRepository).save(pendente);
    verify(orcamentoRepository, never()).save(jaFinalizado);
  }

  @Test
  @DisplayName("Deve cancelar orçamento por ID com sucesso")
  void deveCancelarOrcamentoPorId() {
    UUID id = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    orcamento.setId(id);
    orcamento.setStatus(StatusOrcamento.GERADO);

    when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));
    when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(i -> i.getArgument(0));

    Orcamento cancelado = orcamentoService.cancelar(id);

    assertThat(cancelado.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);
    verify(orcamentoRepository).save(orcamento);
  }

  @Test
  @DisplayName("Deve lançar exceção ao cancelar orçamento inexistente")
  void deveLancarExcecaoAoCancelarOrcamentoInexistente() {
    UUID id = UUID.randomUUID();
    when(orcamentoRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orcamentoService.cancelar(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Orçamento não encontrado");
  }

  @Test
  @DisplayName("Deve lançar exceção ao recuperar PDF de orçamento inexistente")
  void deveLancarExcecaoAoRecuperarPdfOrcamentoInexistente() {
    UUID id = UUID.randomUUID();
    when(orcamentoRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orcamentoService.recuperarPdf(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Orçamento não encontrado");
  }

  @Test
  @DisplayName("Deve lançar exceção ao recuperar PDF com OS inexistente")
  void deveLancarExcecaoAoRecuperarPdfOsInexistente() {
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findById(osId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orcamentoService.recuperarPdf(orcamentoId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Ordem de Serviço associada não encontrada");
  }

  @Test
  @DisplayName("Deve lançar exceção ao recuperar PDF com Cliente inexistente")
  void deveLancarExcecaoAoRecuperarPdfClienteInexistente() {
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setClienteId(clienteId);

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findById(osId)).thenReturn(Optional.of(os));
    when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orcamentoService.recuperarPdf(orcamentoId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Cliente associado não encontrado");
  }

  @Test
  @DisplayName("Deve lançar exceção ao recuperar PDF com Veículo inexistente")
  void deveLancarExcecaoAoRecuperarPdfVeiculoInexistente() {
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setClienteId(clienteId);
    os.setVeiculoId(veiculoId);

    Cliente cliente = new Cliente();
    cliente.setId(clienteId);

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findById(osId)).thenReturn(Optional.of(os));
    when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
    when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orcamentoService.recuperarPdf(orcamentoId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Veículo associado não encontrado");
  }

  @Test
  @DisplayName("Deve aprovar orçamento mesmo com lista de itens vazia (sem baixar estoque)")
  void deveAprovarOrcamentoComItensVazios() {
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);
    orcamento.setStatus(StatusOrcamento.GERADO);

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setItens(Collections.emptyList());

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findByIdWithItens(osId)).thenReturn(Optional.of(os));
    when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(i -> i.getArgument(0));

    Orcamento aprovado = orcamentoService.aprovar(orcamentoId);

    assertThat(aprovado.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    verifyNoInteractions(estoqueGateway);
    verify(orcamentoRepository).save(orcamento);
  }

  @Test
  @DisplayName("Deve falhar aprovação quando lista de itens é nula")
  void deveFalharAprovacaoQuandoItensNulos() {
    UUID orcamentoId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();

    Orcamento orcamento = new Orcamento();
    orcamento.setId(orcamentoId);
    orcamento.setOrdemServicoId(osId);

    OrdemServico os = new OrdemServico();
    os.setId(osId);
    os.setItens(null); // Forcing null list

    when(orcamentoRepository.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
    when(ordemServicoRepository.findByIdWithItens(osId)).thenReturn(Optional.of(os));

    // When OS items are null, it should log a warning and then fail with NPE when trying to iterate
    // The service catches RuntimeException and rethrows it
    assertThatThrownBy(() -> orcamentoService.aprovar(orcamentoId))
        .isInstanceOf(RuntimeException.class);

    verify(orcamentoRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve calcular totais corretamente ignorando tipos não solicitados")
  void deveGerarOrcamentoCalculandoCorretamenteComTiposMisturados() {
    UUID osId = UUID.randomUUID();

    // Item PECA (deve somar em materiais)
    ItemOrdemServico peca =
        ItemOrdemServico.builder()
            .tipo(TipoItem.PECA)
            .valorUnitario(new BigDecimal("10.00"))
            .quantidade(1)
            .build(); // 10.00

    // Item INSUMO (deve somar em materiais)
    ItemOrdemServico insumo =
        ItemOrdemServico.builder()
            .tipo(TipoItem.INSUMO)
            .valorUnitario(new BigDecimal("5.00"))
            .quantidade(1)
            .build(); // 5.00

    // Item SERVICO (deve somar em mao de obra)
    ItemOrdemServico servico =
        ItemOrdemServico.builder()
            .tipo(TipoItem.SERVICO)
            .valorUnitario(new BigDecimal("50.00"))
            .quantidade(1)
            .build(); // 50.00

    OrdemServico os = OrdemServico.builder().id(osId).itens(List.of(peca, insumo, servico)).build();

    when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(i -> i.getArgument(0));
    when(orcamentoRepository.existsByOrdemServicoIdAndStatus(osId, StatusOrcamento.GERADO))
        .thenReturn(false);

    Orcamento orcamento = orcamentoService.gerarOrcamento(os);

    // Materiais: 10 + 5 = 15
    assertThat(orcamento.getValorTotalMateriais()).isEqualByComparingTo(new BigDecimal("15.00"));

    // Mão de Obra: 50
    assertThat(orcamento.getValorTotalMaoDeObra()).isEqualByComparingTo(new BigDecimal("50.00"));
  }

  @Test
  @DisplayName("Deve buscar orçamento por código com sucesso")
  void deveBuscarPorCodigoComSucesso() {
    String codigo = "ORC-ABCD1234";
    Orcamento orcamento = new Orcamento();
    orcamento.setCodigo(codigo);
    when(orcamentoRepository.findByCodigo(codigo)).thenReturn(Optional.of(orcamento));

    Optional<Orcamento> result = orcamentoService.buscarPorCodigo(codigo);

    assertThat(result).isPresent();
    assertThat(result.get().getCodigo()).isEqualTo(codigo);
    verify(orcamentoRepository).findByCodigo(codigo);
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando código de orçamento não existe")
  void deveRetornarVazioQuandoCodigoOrcamentoNaoEncontrado() {
    String codigo = "ORC-INEXISTENTE";
    when(orcamentoRepository.findByCodigo(codigo)).thenReturn(Optional.empty());

    Optional<Orcamento> result = orcamentoService.buscarPorCodigo(codigo);

    assertThat(result).isEmpty();
  }
}
