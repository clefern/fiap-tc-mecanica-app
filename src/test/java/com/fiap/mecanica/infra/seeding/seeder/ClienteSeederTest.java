package com.fiap.mecanica.infra.seeding.seeder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.ClienteService;
import com.fiap.mecanica.application.service.VeiculoService;
import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.infra.seeding.SeedingConstants;
import com.fiap.mecanica.infra.seeding.factory.ClienteFactory;
import com.fiap.mecanica.infra.seeding.factory.VeiculoFactory;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ClienteSeederTest {

  @Mock private ClienteFactory clienteFactory;
  @Mock private VeiculoFactory veiculoFactory;
  @Mock private ClienteService clienteService;
  @Mock private VeiculoService veiculoService;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private ClienteSeeder clienteSeeder;

  private Cliente mockCliente;
  private Veiculo mockVeiculo;

  @BeforeEach
  void setUp() {
    lenient().when(passwordEncoder.encode(anyString())).thenReturn("hash");

    mockCliente = mock(Cliente.class);
    UUID clienteId = UUID.randomUUID();
    lenient().when(mockCliente.getId()).thenReturn(clienteId);
    lenient()
        .when(mockCliente.getDocumento())
        .thenReturn(CPF.of(SeedingConstants.FIXED_CLIENTE_CPF));

    mockVeiculo = mock(Veiculo.class);
    lenient().when(mockVeiculo.getId()).thenReturn(UUID.randomUUID());

    lenient()
        .when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient().when(clienteFactory.create(any(TipoPessoa.class))).thenReturn(mockCliente);
    lenient().when(clienteService.create(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
    lenient()
        .when(clienteService.update(any(), any(Cliente.class)))
        .thenAnswer(i -> i.getArgument(1));
    lenient().when(clienteService.getByDocumento(anyString())).thenReturn(Optional.empty());
    lenient().when(veiculoService.create(any(), any(Veiculo.class))).thenReturn(mockVeiculo);
    lenient().when(veiculoFactory.create()).thenReturn(mockVeiculo);
    lenient().when(veiculoService.listByClienteId(any())).thenReturn(Collections.emptyList());
    lenient().when(veiculoService.getByPlaca(anyString())).thenReturn(Optional.empty());
  }

  @Test
  @DisplayName("Should skip cliente seeding when count is at or above threshold of 10")
  void shouldSkipClienteSeedingWhenEnoughExist() {
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(10, mockCliente)));

    clienteSeeder.seed();

    verify(clienteFactory, never()).create(any(TipoPessoa.class));
  }

  @Test
  @DisplayName("Should seed 10 clientes with exact 50/50 PF/PJ split when DB is empty")
  void shouldSeedClientesWithFiftyFiftySplit() {
    clienteSeeder.seed();

    verify(clienteFactory, times(5)).create(TipoPessoa.FISICA);
    verify(clienteFactory, times(5)).create(TipoPessoa.JURIDICA);
  }

  @Test
  @DisplayName("Should create fixed cliente when CPF not found in DB")
  void shouldCreateFixedClienteWhenNotExists() {
    when(clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF))
        .thenReturn(Optional.empty());

    Cliente saved = mock(Cliente.class);
    when(saved.getId()).thenReturn(UUID.randomUUID());
    when(clienteService.create(any(Cliente.class))).thenReturn(saved);
    when(clienteService.update(any(), any(Cliente.class))).thenReturn(saved);

    clienteSeeder.seed();

    verify(clienteService, atLeast(1)).create(any(Cliente.class));
    verify(clienteService).update(any(), any(Cliente.class));
  }

  @Test
  @DisplayName("Should skip fixed cliente creation when CPF already exists")
  void shouldSkipFixedClienteWhenAlreadyExists() {
    Cliente existing = mock(Cliente.class);
    when(clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF))
        .thenReturn(Optional.of(existing));

    clienteSeeder.seed();

    // update should never be called — fixed cliente already existed
    verify(clienteService, never()).update(any(), any(Cliente.class));
  }

  @Test
  @DisplayName("Should skip veiculo seeding entirely when no clientes exist in DB")
  void shouldSkipVeiculoSeedingWhenNoClientes() {
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    clienteSeeder.seedVeiculos();

    verify(veiculoService, never()).create(any(), any(Veiculo.class));
  }

  @Test
  @DisplayName("Should create one mandatory vehicle per cliente that has none")
  void shouldCreateMandatoryVehicleForClienteWithoutVehicles() {
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockCliente)));
    when(veiculoService.listByClienteId(mockCliente.getId())).thenReturn(Collections.emptyList());

    // Override to prevent extra vehicles and isolate the mandatory vehicle assertion
    ClienteSeeder noExtraSeeder =
        new ClienteSeeder(
            clienteFactory, veiculoFactory, clienteService, veiculoService, passwordEncoder) {
          @Override
          protected boolean shouldCreateExtraVehicles() {
            return false;
          }
        };

    noExtraSeeder.seedVeiculos();

    verify(veiculoService, atLeast(1)).create(eq(mockCliente.getId()), any(Veiculo.class));
  }

  @Test
  @DisplayName("Should skip vehicle creation for clientes that already have vehicles")
  void shouldSkipVehicleCreationWhenClienteAlreadyHasVehicles() {
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockCliente)));
    when(veiculoService.listByClienteId(mockCliente.getId())).thenReturn(List.of(mockVeiculo));

    clienteSeeder.seedVeiculos();

    verify(veiculoService, never()).create(eq(mockCliente.getId()), any(Veiculo.class));
  }

  @Test
  @DisplayName("Should create extra vehicles when chance override returns true")
  void shouldCreateExtraVehiclesWhenChanceHits() {
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockCliente)));
    when(veiculoService.listByClienteId(mockCliente.getId())).thenReturn(Collections.emptyList());

    ClienteSeeder alwaysExtraSeeder =
        new ClienteSeeder(
            clienteFactory, veiculoFactory, clienteService, veiculoService, passwordEncoder) {
          @Override
          protected boolean shouldCreateExtraVehicles() {
            return true;
          }

          @Override
          protected int getNumberOfExtraVehicles() {
            return 2;
          }
        };

    alwaysExtraSeeder.seedVeiculos();

    // 1 mandatory + 2 extra = 3 for mockCliente (fixed veiculo is also created separately)
    verify(veiculoService, times(3)).create(eq(mockCliente.getId()), any(Veiculo.class));
  }

  @Test
  @DisplayName("Should create fixed veiculo ABC1D23 when it does not exist")
  void shouldCreateFixedVeiculoWhenNotExists() {
    // seedVeiculos exits early when clientes list is empty — need at least one client
    // Make mockCliente already have a vehicle so the loop skips it,
    // then ensureFixedVeiculo runs and creates the fixed plate
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockCliente)));
    when(veiculoService.listByClienteId(mockCliente.getId()))
        .thenReturn(List.of(mockVeiculo)); // loop: already has vehicles, skip
    when(veiculoService.getByPlaca(SeedingConstants.FIXED_VEICULO_PLACA))
        .thenReturn(Optional.empty());
    when(clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF))
        .thenReturn(Optional.of(mockCliente));

    clienteSeeder.seedVeiculos();

    UUID capturedId = mockCliente.getId();
    verify(veiculoService).create(eq(capturedId), any(Veiculo.class));
  }

  @Test
  @DisplayName("Should skip fixed veiculo creation when plate ABC1D23 already exists")
  void shouldSkipFixedVeiculoWhenAlreadyExists() {
    // Need a non-empty list so seedVeiculos does not return early
    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockCliente)));
    when(veiculoService.listByClienteId(mockCliente.getId()))
        .thenReturn(List.of(mockVeiculo)); // loop: already has vehicles, skip
    when(veiculoService.getByPlaca(SeedingConstants.FIXED_VEICULO_PLACA))
        .thenReturn(Optional.of(mockVeiculo)); // fixed veiculo already exists

    clienteSeeder.seedVeiculos();

    // No additional vehicle created (loop skipped, fixed already exists)
    verify(veiculoService, never()).create(any(), any(Veiculo.class));
  }

  @Test
  @DisplayName("shouldCreateExtraVehicles returns a boolean without throwing")
  void shouldCreateExtraVehiclesReturnsBooleanSafely() {
    boolean result = clienteSeeder.shouldCreateExtraVehicles();
    assertThat(result).isIn(true, false);
  }

  @Test
  @DisplayName("getNumberOfExtraVehicles returns a value between 1 and 2 inclusive")
  void getNumberOfExtraVehiclesReturnsBetweenOneAndTwo() {
    int extras = clienteSeeder.getNumberOfExtraVehicles();
    assertThat(extras).isBetween(1, 2);
  }

  @Test
  @DisplayName("Should handle factory exception during cliente creation gracefully")
  void shouldHandleClienteCreationExceptionGracefully() {
    when(clienteFactory.create(any(TipoPessoa.class)))
        .thenThrow(new RuntimeException("Invalid CPF generated"));

    // Should NOT propagate
    clienteSeeder.seed();
  }
}
