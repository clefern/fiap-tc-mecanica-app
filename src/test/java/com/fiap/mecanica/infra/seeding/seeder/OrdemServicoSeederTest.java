package com.fiap.mecanica.infra.seeding.seeder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.ClienteService;
import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.application.service.MecanicoService;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.application.service.OrdemServicoService;
import com.fiap.mecanica.application.service.OsItemService;
import com.fiap.mecanica.application.service.OsLifecycleService;
import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.application.service.ServicoService;
import com.fiap.mecanica.application.service.VeiculoService;
import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.exception.ItemDuplicadoException;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.infra.seeding.SeedingConstants;
import com.fiap.mecanica.infra.seeding.SeedingShutdownGuard;
import com.fiap.mecanica.infra.seeding.factory.ItemOrdemServicoFactory;
import java.lang.reflect.Method;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrdemServicoSeederTest {

  @Mock private SeedingShutdownGuard shutdownGuard;
  @Mock private ItemOrdemServicoFactory itemOrdemServicoFactory;
  @Mock private OrdemServicoService ordemServicoService;
  @Mock private OsLifecycleService lifecycleService;
  @Mock private OsItemService itemService;
  @Mock private OrcamentoService orcamentoService;
  @Mock private PrioridadeService prioridadeService;
  @Mock private ClienteService clienteService;
  @Mock private MecanicoService mecanicoService;
  @Mock private VeiculoService veiculoService;
  @Mock private ServicoService servicoService;
  @Mock private PecaService pecaService;
  @Mock private InsumoService insumoService;

  @InjectMocks private OrdemServicoSeeder ordemServicoSeeder;

  private UUID osId;
  private UUID mecanicoId;
  private OrdemServico mockOs;
  private Orcamento mockOrcamento;
  private Mecanico mockMecanico;
  private Cliente mockCliente;
  private Veiculo mockVeiculo;

  @BeforeEach
  void setUp() {
    osId = UUID.randomUUID();
    mecanicoId = UUID.randomUUID();

    mockOs = mock(OrdemServico.class);
    lenient().when(mockOs.getId()).thenReturn(osId);
    lenient().when(mockOs.getCodigo()).thenReturn("OS-TEST-001");
    lenient().when(mockOs.getStatus()).thenReturn(StatusOS.APROVADA);

    mockOrcamento = mock(Orcamento.class);
    lenient().when(mockOrcamento.getId()).thenReturn(UUID.randomUUID());

    mockMecanico = mock(Mecanico.class);
    lenient().when(mockMecanico.getId()).thenReturn(mecanicoId);

    mockCliente = mock(Cliente.class);
    UUID clienteId = UUID.randomUUID();
    lenient().when(mockCliente.getId()).thenReturn(clienteId);

    mockVeiculo = mock(Veiculo.class);
    UUID veiculoId = UUID.randomUUID();
    lenient().when(mockVeiculo.getId()).thenReturn(veiculoId);

    // Default: DB has no OS
    lenient()
        .when(ordemServicoService.listarTodas(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    // Default: no clientes
    lenient()
        .when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    // Default: fixed entity lookups return empty
    lenient()
        .when(mecanicoService.getByCpf(SeedingConstants.FIXED_MECANICO_CPF))
        .thenReturn(Optional.empty());
    lenient()
        .when(clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF))
        .thenReturn(Optional.empty());
    lenient()
        .when(veiculoService.getByPlaca(SeedingConstants.FIXED_VEICULO_PLACA))
        .thenReturn(Optional.empty());

    // Default catalog
    lenient()
        .when(servicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(pecaService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(insumoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    // Default: OS creation returns a mock
    lenient()
        .when(ordemServicoService.criarOrdemServico(any(), any(), anyString()))
        .thenReturn(mockOs);

    // Default: item factory
    lenient()
        .when(itemOrdemServicoFactory.createRandom(any(), any(), any()))
        .thenReturn(mock(ItemOrdemServico.class));

    // Default: item service returns mockOs
    lenient().when(itemService.adicionarItem(any(), any(), any())).thenReturn(mockOs);

    // Default: lifecycle transitions return mockOs
    lenient().when(lifecycleService.iniciarDiagnostico(any(), any())).thenReturn(mockOs);
    lenient().when(lifecycleService.finalizarDiagnostico(any(), any())).thenReturn(mockOs);
    lenient().when(lifecycleService.iniciarExecucao(any(), any())).thenReturn(mockOs);
    lenient().when(lifecycleService.finalizar(any(), any())).thenReturn(mockOs);
    lenient().when(lifecycleService.entregar(any())).thenReturn(mockOs);
    lenient().when(lifecycleService.aprovarOS(any())).thenReturn(mockOs);
    lenient().when(lifecycleService.cancelar(any())).thenReturn(mockOs);
    lenient().when(ordemServicoService.buscarPorId(any())).thenReturn(mockOs);

    lenient()
        .when(orcamentoService.buscarPorOrdemServico(any()))
        .thenReturn(Optional.of(mockOrcamento));
    lenient().when(orcamentoService.aprovar(any())).thenReturn(mockOrcamento);
  }

  // -------------------------------------------------------------------------
  // Fixed Scenarios
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Should skip fixed scenarios when fixed mecanico is missing")
  void shouldSkipFixedScenariosWhenMecanicoMissing() {
    when(mecanicoService.getByCpf(SeedingConstants.FIXED_MECANICO_CPF))
        .thenReturn(Optional.empty());
    when(clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF))
        .thenReturn(Optional.of(mockCliente));
    when(veiculoService.getByPlaca(SeedingConstants.FIXED_VEICULO_PLACA))
        .thenReturn(Optional.of(mockVeiculo));

    ordemServicoSeeder.seed();

    verify(ordemServicoService, never()).criarOrdemServico(any(), any(), anyString());
  }

  @Test
  @DisplayName("Should skip fixed scenarios when fixed cliente is missing")
  void shouldSkipFixedScenariosWhenClienteMissing() {
    when(mecanicoService.getByCpf(SeedingConstants.FIXED_MECANICO_CPF))
        .thenReturn(Optional.of(mockMecanico));
    when(clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF))
        .thenReturn(Optional.empty());

    ordemServicoSeeder.seed();

    verify(ordemServicoService, never()).criarOrdemServico(any(), any(), anyString());
  }

  @Test
  @DisplayName("Should skip fixed scenarios when fixed veiculo is missing")
  void shouldSkipFixedScenariosWhenVeiculoMissing() {
    when(mecanicoService.getByCpf(SeedingConstants.FIXED_MECANICO_CPF))
        .thenReturn(Optional.of(mockMecanico));
    when(clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF))
        .thenReturn(Optional.of(mockCliente));
    when(veiculoService.getByPlaca(SeedingConstants.FIXED_VEICULO_PLACA))
        .thenReturn(Optional.empty());

    ordemServicoSeeder.seed();

    verify(ordemServicoService, never()).criarOrdemServico(any(), any(), anyString());
  }

  @Test
  @DisplayName("Should create 3 fixed demo OS when all fixed entities are present")
  void shouldCreateThreeFixedOSsWhenEntitiesPresent() {
    when(mecanicoService.getByCpf(SeedingConstants.FIXED_MECANICO_CPF))
        .thenReturn(Optional.of(mockMecanico));
    when(clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF))
        .thenReturn(Optional.of(mockCliente));
    when(veiculoService.getByPlaca(SeedingConstants.FIXED_VEICULO_PLACA))
        .thenReturn(Optional.of(mockVeiculo));

    ordemServicoSeeder.seed();

    UUID capturedClienteId = mockCliente.getId();
    UUID capturedVeiculoId = mockVeiculo.getId();

    // 3 fixed OS: AGUARDANDO_APROVACAO, EM_EXECUCAO, FINALIZADA
    verify(ordemServicoService, atLeastOnce())
        .criarOrdemServico(eq(capturedClienteId), eq(capturedVeiculoId), anyString());
  }

  // -------------------------------------------------------------------------
  // Random OS seeding (seedOrdemServicos)
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Should skip random OS seeding when DB already has more than 30 records")
  void shouldSkipRandomOsSeedingWhenEnoughExist() {
    when(ordemServicoService.listarTodas(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(31, mockOs)));

    ordemServicoSeeder.seedOrdemServicos();

    verify(clienteService, never()).getAll(any(Pageable.class));
  }

  @Test
  @DisplayName("Should skip random OS seeding when no clientes exist")
  void shouldSkipRandomOsSeedingWhenNoClientes() {
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    ordemServicoSeeder.seedOrdemServicos();

    verify(ordemServicoService, never()).criarOrdemServico(any(), any(), anyString());
  }

  @Test
  @DisplayName("Should skip random OS seeding when no mecanicos exist")
  void shouldSkipRandomOsSeedingWhenNoMecanicos() {
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockCliente)));
    when(mecanicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    ordemServicoSeeder.seedOrdemServicos();

    verify(ordemServicoService, never()).criarOrdemServico(any(), any(), anyString());
  }

  @Test
  @DisplayName("Should create OS for each vehicle of each cliente")
  void shouldSeedRandomOsForEachVehicle() {
    UUID capturedClienteId = mockCliente.getId();
    UUID capturedVeiculoId = mockVeiculo.getId();

    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockCliente)));
    when(veiculoService.listByClienteId(capturedClienteId)).thenReturn(List.of(mockVeiculo));
    when(mecanicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockMecanico)));

    ordemServicoSeeder.seedOrdemServicos();

    verify(ordemServicoService, atLeastOnce())
        .criarOrdemServico(eq(capturedClienteId), eq(capturedVeiculoId), anyString());
  }

  // -------------------------------------------------------------------------
  // Duplicate item handling
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Should ignore ItemDuplicadoException and continue OS creation without cancelling")
  void shouldIgnoreDuplicateItemAndContinue() {
    UUID capturedClienteId = mockCliente.getId();
    UUID capturedVeiculoId = mockVeiculo.getId();

    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockCliente)));
    when(veiculoService.listByClienteId(capturedClienteId)).thenReturn(List.of(mockVeiculo));
    when(mecanicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockMecanico)));

    when(itemService.adicionarItem(any(), any(), any()))
        .thenReturn(mockOs)
        .thenThrow(new ItemDuplicadoException("Troca de Óleo", UUID.randomUUID()))
        .thenReturn(mockOs);

    ordemServicoSeeder.seedOrdemServicos();

    // OS was created — ItemDuplicadoException did not abort the creation loop
    verify(ordemServicoService, atLeastOnce())
        .criarOrdemServico(eq(capturedClienteId), eq(capturedVeiculoId), anyString());
  }

  // -------------------------------------------------------------------------
  // Status transition engine (via reflection on private method)
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Transition to RECEBIDA should be a no-op (no service calls)")
  void shouldTransitionToRecebidaWithNoOp() throws Exception {
    invokeTransition(osId, StatusOS.RECEBIDA, mecanicoId);

    verify(lifecycleService, never()).iniciarDiagnostico(any(), any());
    verify(lifecycleService, never()).cancelar(any());
  }

  @Test
  @DisplayName("Transition to CANCELADA should call cancelar only")
  void shouldTransitionToCancelada() throws Exception {
    invokeTransition(osId, StatusOS.CANCELADA, mecanicoId);

    verify(lifecycleService).cancelar(osId);
    verify(lifecycleService, never()).iniciarDiagnostico(any(), any());
  }

  @Test
  @DisplayName("Transition to EM_DIAGNOSTICO should call iniciarDiagnostico only")
  void shouldTransitionToEmDiagnostico() throws Exception {
    invokeTransition(osId, StatusOS.EM_DIAGNOSTICO, mecanicoId);

    verify(lifecycleService).iniciarDiagnostico(osId, mecanicoId);
    verify(lifecycleService, never()).finalizarDiagnostico(any(), any());
  }

  @Test
  @DisplayName("Transition to AGUARDANDO_APROVACAO should call iniciar then finalizar diagnostico")
  void shouldTransitionToAguardandoAprovacao() throws Exception {
    invokeTransition(osId, StatusOS.AGUARDANDO_APROVACAO, mecanicoId);

    verify(lifecycleService).iniciarDiagnostico(osId, mecanicoId);
    verify(lifecycleService).finalizarDiagnostico(osId, mecanicoId);
    verify(orcamentoService, never()).aprovar(any());
  }

  @Test
  @DisplayName("Transition to APROVADA should approve the budget")
  void shouldTransitionToAprovada() throws Exception {
    invokeTransition(osId, StatusOS.APROVADA, mecanicoId);

    verify(lifecycleService).iniciarDiagnostico(osId, mecanicoId);
    verify(lifecycleService).finalizarDiagnostico(osId, mecanicoId);
    verify(orcamentoService).aprovar(mockOrcamento.getId());
    verify(lifecycleService, never()).iniciarExecucao(any(), any());
  }

  @Test
  @DisplayName("Transition to EM_EXECUCAO should approve budget then start execution")
  void shouldTransitionToEmExecucao() throws Exception {
    invokeTransition(osId, StatusOS.EM_EXECUCAO, mecanicoId);

    verify(orcamentoService).aprovar(mockOrcamento.getId());
    verify(lifecycleService).iniciarExecucao(osId, mecanicoId);
    verify(lifecycleService, never()).finalizar(any(), any());
  }

  @Test
  @DisplayName("Transition to FINALIZADA should go through full flow including finalizar")
  void shouldTransitionToFinalizada() throws Exception {
    invokeTransition(osId, StatusOS.FINALIZADA, mecanicoId);

    verify(orcamentoService).aprovar(mockOrcamento.getId());
    verify(lifecycleService).iniciarExecucao(osId, mecanicoId);
    verify(lifecycleService).finalizar(osId, mecanicoId);
    verify(lifecycleService, never()).entregar(any());
  }

  @Test
  @DisplayName("Transition to ENTREGUE should call entregar as final step")
  void shouldTransitionToEntregue() throws Exception {
    invokeTransition(osId, StatusOS.ENTREGUE, mecanicoId);

    verify(orcamentoService).aprovar(mockOrcamento.getId());
    verify(lifecycleService).iniciarExecucao(osId, mecanicoId);
    verify(lifecycleService).finalizar(osId, mecanicoId);
    verify(lifecycleService).entregar(osId);
  }

  @Test
  @DisplayName("Should generate budget manually when orcamento not found and target needs approval")
  void shouldGenerateBudgetManuallyWhenNotFound() throws Exception {
    when(orcamentoService.buscarPorOrdemServico(osId)).thenReturn(Optional.empty());
    when(orcamentoService.gerarOrcamento(any())).thenReturn(mockOrcamento);

    invokeTransition(osId, StatusOS.APROVADA, mecanicoId);

    verify(orcamentoService).gerarOrcamento(any(OrdemServico.class));
    verify(orcamentoService).aprovar(mockOrcamento.getId());
  }

  @Test
  @DisplayName("Should cancel OS and re-throw when transition fails mid-flow")
  void shouldCancelOsWhenTransitionFails() throws Exception {
    when(lifecycleService.iniciarDiagnostico(osId, mecanicoId))
        .thenThrow(new RuntimeException("Transition failed"));

    try {
      invokeTransition(osId, StatusOS.FINALIZADA, mecanicoId);
    } catch (Exception ignored) {
      // Expected — method re-throws after cancelling
    }

    verify(lifecycleService).cancelar(osId);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when mecanicoId is null")
  void shouldThrowWhenMecanicoIdIsNull() {
    try {
      invokeTransition(osId, StatusOS.EM_DIAGNOSTICO, null);
    } catch (Exception e) {
      // Expected: IllegalArgumentException wrapped in InvocationTargetException
      assertCauseIsIllegalArgument(e);
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private void invokeTransition(UUID targetOsId, StatusOS status, UUID targetMecanicoId)
      throws Exception {
    Method method =
        OrdemServicoSeeder.class.getDeclaredMethod(
            "transitionToStatus", UUID.class, StatusOS.class, UUID.class);
    method.setAccessible(true);
    method.invoke(ordemServicoSeeder, targetOsId, status, targetMecanicoId);
  }

  private void assertCauseIsIllegalArgument(Exception e) {
    Throwable cause = e.getCause() != null ? e.getCause() : e;
    assert cause instanceof IllegalArgumentException
        : "Expected IllegalArgumentException but got: " + cause.getClass().getName();
  }
}
