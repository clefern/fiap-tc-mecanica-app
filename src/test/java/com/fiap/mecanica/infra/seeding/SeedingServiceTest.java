package com.fiap.mecanica.infra.seeding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.AtendenteService;
import com.fiap.mecanica.application.service.ClienteService;
import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.application.service.MecanicoService;
import com.fiap.mecanica.application.service.OrdemServicoService;
import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.application.service.ServicoService;
import com.fiap.mecanica.application.service.VeiculoService;
import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.infra.seeding.seeder.CatalogoSeeder;
import com.fiap.mecanica.infra.seeding.seeder.ClienteSeeder;
import com.fiap.mecanica.infra.seeding.seeder.OrdemServicoSeeder;
import com.fiap.mecanica.infra.seeding.seeder.UsuarioSeeder;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Tests for {@link SeedingOrchestrator}. Verifies seeding is delegated to each seeder in the
 * correct order and that verification logic runs without errors.
 */
@ExtendWith(MockitoExtension.class)
class SeedingServiceTest {

  @Mock private SeedingShutdownGuard shutdownGuard;
  @Mock private UsuarioSeeder usuarioSeeder;
  @Mock private CatalogoSeeder catalogoSeeder;
  @Mock private ClienteSeeder clienteSeeder;
  @Mock private OrdemServicoSeeder ordemServicoSeeder;

  @Mock private ClienteService clienteService;
  @Mock private VeiculoService veiculoService;
  @Mock private MecanicoService mecanicoService;
  @Mock private AtendenteService atendenteService;
  @Mock private ServicoService servicoService;
  @Mock private PecaService pecaService;
  @Mock private InsumoService insumoService;
  @Mock private OrdemServicoService ordemServicoService;

  @InjectMocks private SeedingOrchestrator orchestrator;

  @BeforeEach
  void setUp() {
    lenient()
        .when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(veiculoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(mecanicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(atendenteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(servicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(pecaService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(insumoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(ordemServicoService.listarTodas(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
  }

  @Test
  @DisplayName("Should delegate to all seeders in correct order")
  void shouldDelegateToAllSeedersInOrder() {
    orchestrator.seed();

    InOrder order = inOrder(usuarioSeeder, catalogoSeeder, clienteSeeder, ordemServicoSeeder);
    order.verify(usuarioSeeder).seed();
    order.verify(catalogoSeeder).seed();
    order.verify(clienteSeeder).seed();
    order.verify(ordemServicoSeeder).seed();
  }

  @Test
  @DisplayName("Should run verifySeeding without error when no clients exist")
  void shouldVerifySeedingWithNoClients() throws Exception {
    Method verifyMethod = SeedingOrchestrator.class.getDeclaredMethod("verifySeeding");
    verifyMethod.setAccessible(true);
    verifyMethod.invoke(orchestrator);

    verify(clienteService).getAll(any(Pageable.class));
  }

  @Test
  @DisplayName("Should verify distribution and flag clients without vehicles")
  void shouldVerifyDistributionAndVehicles() throws Exception {
    Cliente clienteFisica = mock(Cliente.class);
    lenient().when(clienteFisica.getTipo()).thenReturn(TipoPessoa.FISICA);

    Cliente clienteJuridica = mock(Cliente.class);
    lenient().when(clienteJuridica.getTipo()).thenReturn(TipoPessoa.JURIDICA);

    when(clienteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(clienteFisica, clienteJuridica)));
    when(veiculoService.listByClienteId(any())).thenReturn(Collections.emptyList());

    Method verifyMethod = SeedingOrchestrator.class.getDeclaredMethod("verifySeeding");
    verifyMethod.setAccessible(true);
    verifyMethod.invoke(orchestrator);
  }
}
