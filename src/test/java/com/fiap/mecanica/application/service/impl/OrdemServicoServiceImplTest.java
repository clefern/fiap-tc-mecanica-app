package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.events.OsCriadaEvent;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.application.service.os.OsEntityValidator;
import com.fiap.mecanica.application.service.os.OsEstoqueValidator;
import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.domain.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.domain.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceImplTest {

  @Mock private OrdemServicoRepository repository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private OrcamentoService orcamentoService;
  @Mock private PrioridadeService prioridadeService;
  @Mock private OsEntityValidator entityValidator;
  @Mock private OsEstoqueValidator estoqueValidator;

  @InjectMocks private OrdemServicoServiceImpl service;

  private UUID clienteId;
  private UUID veiculoId;
  private String observacoes;
  private UUID osId;
  private OrdemServico os;

  @BeforeEach
  void setUp() {
    clienteId = UUID.randomUUID();
    veiculoId = UUID.randomUUID();
    observacoes = "Test observation";
    osId = UUID.randomUUID();

    os = OrdemServico.nova(clienteId, veiculoId);
    os.setId(osId);
  }

  @Test
  @DisplayName("Should create Ordem Servico successfully")
  void shouldCreateOrdemServicoSuccessfully() {
    when(repository.save(any(OrdemServico.class)))
        .thenAnswer(
            i -> {
              OrdemServico saved = i.getArgument(0);
              saved.setId(osId);
              return saved;
            });

    OrdemServico result = service.criarOrdemServico(clienteId, veiculoId, observacoes);

    assertThat(result).isNotNull();
    assertThat(result.getClienteId()).isEqualTo(clienteId);
    assertThat(result.getVeiculoId()).isEqualTo(veiculoId);
    assertThat(result.getObservacoes()).isEqualTo(observacoes);
    verify(entityValidator).validar(clienteId, veiculoId);
    verify(eventPublisher).publishEvent(any(OsCriadaEvent.class));
  }

  @Test
  @DisplayName("Should throw ClienteNaoEncontradoException when entity validation fails")
  void shouldThrowExceptionWhenCreatingOsWithInvalidClient() {
    doThrow(new ClienteNaoEncontradoException(clienteId))
        .when(entityValidator)
        .validar(clienteId, veiculoId);

    assertThatThrownBy(() -> service.criarOrdemServico(clienteId, veiculoId, observacoes))
        .isInstanceOf(ClienteNaoEncontradoException.class);
  }

  @Test
  @DisplayName("Should throw VeiculoNaoEncontradoException when entity validation fails")
  void shouldThrowExceptionWhenCreatingOsWithInvalidVehicle() {
    doThrow(new VeiculoNaoEncontradoException("Veículo não encontrado"))
        .when(entityValidator)
        .validar(clienteId, veiculoId);

    assertThatThrownBy(() -> service.criarOrdemServico(clienteId, veiculoId, observacoes))
        .isInstanceOf(VeiculoNaoEncontradoException.class);
  }

  @Test
  @DisplayName("Should find Ordem Servico by ID")
  void shouldFindOrdemServicoById() {
    when(repository.findById(osId)).thenReturn(Optional.of(os));

    OrdemServico result = service.buscarPorId(osId);

    assertThat(result).isEqualTo(os);
  }

  @Test
  @DisplayName("Should throw exception when OS not found by ID")
  void shouldThrowExceptionWhenOsNotFoundById() {
    when(repository.findById(osId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.buscarPorId(osId))
        .isInstanceOf(OrdemServicoNaoEncontradaException.class);
  }

  @Test
  @DisplayName("Should list all Ordem Servicos")
  void shouldListAllOrdemServicos() {
    Pageable pageable = Pageable.unpaged();
    Page<OrdemServico> page = new PageImpl<>(Collections.singletonList(os));
    when(repository.findAll(pageable)).thenReturn(page);

    Page<OrdemServico> result = service.listarTodas(null, null, pageable);

    assertThat(result).isEqualTo(page);
  }

  @Test
  @DisplayName("Deve listar fila operacional delegando para repositório")
  void deveListarFilaOperacionalComSucesso() {
    Page<OrdemServico> page = new PageImpl<>(List.of(os));
    when(repository.listarFilaOperacional(any(Pageable.class))).thenReturn(page);

    Page<OrdemServico> result = service.listarFilaOperacional(Pageable.unpaged());

    assertThat(result.getContent()).hasSize(1);
    verify(repository).listarFilaOperacional(any(Pageable.class));
  }

  @Test
  @DisplayName("Deve buscar OS por código com sucesso")
  void deveBuscarPorCodigoComSucesso() {
    String codigo = "OS-ABCD1234";
    os.setCodigo(codigo);
    when(repository.findByCodigo(codigo)).thenReturn(Optional.of(os));

    OrdemServico result = service.buscarPorCodigo(codigo);

    assertThat(result).isEqualTo(os);
    verify(repository).findByCodigo(codigo);
  }

  @Test
  @DisplayName("Deve lançar OrdemServicoNaoEncontradaException quando código não existe")
  void deveLancarExcecaoQuandoCodigoNaoEncontrado() {
    String codigo = "OS-INEXISTENTE";
    when(repository.findByCodigo(codigo)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.buscarPorCodigo(codigo))
        .isInstanceOf(OrdemServicoNaoEncontradaException.class)
        .hasMessageContaining(codigo);
  }

  @Test
  @DisplayName("abrirOsCompleta: Should create OS with items successfully")
  void abrirOsCompleta_shouldCreateOsWithItemsSuccessfully() {
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Troca de óleo")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();

    when(repository.save(any(OrdemServico.class)))
        .thenAnswer(
            i -> {
              OrdemServico saved = i.getArgument(0);
              saved.setId(osId);
              return saved;
            });

    OrdemServico result = service.abrirOsCompleta(clienteId, veiculoId, observacoes, List.of(item));

    assertThat(result).isNotNull();
    assertThat(result.getClienteId()).isEqualTo(clienteId);
    assertThat(result.getVeiculoId()).isEqualTo(veiculoId);
    assertThat(result.getItens()).hasSize(1);
    verify(entityValidator).validar(clienteId, veiculoId);
    verify(estoqueValidator).validar(item);
    verify(eventPublisher).publishEvent(any(OsCriadaEvent.class));
  }

  @Test
  @DisplayName("abrirOsCompleta: Should create OS with null items list")
  void abrirOsCompleta_shouldCreateOsWithNullItemsList() {
    when(repository.save(any(OrdemServico.class)))
        .thenAnswer(
            i -> {
              OrdemServico saved = i.getArgument(0);
              saved.setId(osId);
              return saved;
            });

    OrdemServico result = service.abrirOsCompleta(clienteId, veiculoId, observacoes, null);

    assertThat(result.getItens()).isEmpty();
    verify(eventPublisher).publishEvent(any(OsCriadaEvent.class));
  }

  @Test
  @DisplayName("abrirOsCompleta: Should create OS with empty items list")
  void abrirOsCompleta_shouldCreateOsWithEmptyItemsList() {
    when(repository.save(any(OrdemServico.class)))
        .thenAnswer(
            i -> {
              OrdemServico saved = i.getArgument(0);
              saved.setId(osId);
              return saved;
            });

    OrdemServico result =
        service.abrirOsCompleta(clienteId, veiculoId, observacoes, Collections.emptyList());

    assertThat(result.getItens()).isEmpty();
    verify(eventPublisher).publishEvent(any(OsCriadaEvent.class));
  }

  @Test
  @DisplayName("abrirOsCompleta: Should throw ClienteNaoEncontradoException when validation fails")
  void abrirOsCompleta_shouldThrowClienteNaoEncontradoException() {
    doThrow(new ClienteNaoEncontradoException(clienteId))
        .when(entityValidator)
        .validar(clienteId, veiculoId);

    assertThatThrownBy(() -> service.abrirOsCompleta(clienteId, veiculoId, observacoes, null))
        .isInstanceOf(ClienteNaoEncontradoException.class);
  }

  @Test
  @DisplayName("abrirOsCompleta: Should throw VeiculoNaoEncontradoException when validation fails")
  void abrirOsCompleta_shouldThrowVeiculoNaoEncontradoException() {
    doThrow(new VeiculoNaoEncontradoException("Veículo não encontrado"))
        .when(entityValidator)
        .validar(clienteId, veiculoId);

    assertThatThrownBy(() -> service.abrirOsCompleta(clienteId, veiculoId, observacoes, null))
        .isInstanceOf(VeiculoNaoEncontradoException.class);
  }

  @Test
  @DisplayName("abrirOsCompleta: Should throw EstoqueInsuficienteException for PECA item")
  void abrirOsCompleta_shouldThrowEstoqueInsuficienteExceptionForPeca() {
    UUID pecaId = UUID.randomUUID();
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.PECA)
            .descricao("Filtro de ar")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(10)
            .referenciaId(pecaId)
            .build();

    doThrow(new EstoqueInsuficienteException("Filtro de ar", 10, 5))
        .when(estoqueValidator)
        .validar(item);

    assertThatThrownBy(
            () -> service.abrirOsCompleta(clienteId, veiculoId, observacoes, List.of(item)))
        .isInstanceOf(EstoqueInsuficienteException.class)
        .hasMessageContaining("Filtro de ar");
  }
}
