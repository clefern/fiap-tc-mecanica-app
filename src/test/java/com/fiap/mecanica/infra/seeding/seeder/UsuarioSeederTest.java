package com.fiap.mecanica.infra.seeding.seeder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.AdminService;
import com.fiap.mecanica.application.service.AtendenteService;
import com.fiap.mecanica.application.service.MecanicoService;
import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.infra.seeding.SeedingConstants;
import com.fiap.mecanica.infra.seeding.factory.AtendenteFactory;
import com.fiap.mecanica.infra.seeding.factory.MecanicoFactory;
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
class UsuarioSeederTest {

  @Mock private MecanicoFactory mecanicoFactory;
  @Mock private AtendenteFactory atendenteFactory;
  @Mock private MecanicoService mecanicoService;
  @Mock private AtendenteService atendenteService;
  @Mock private AdminService adminService;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UsuarioSeeder usuarioSeeder;

  private Mecanico mockMecanico;
  private Atendente mockAtendente;

  @BeforeEach
  void setUp() {
    lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

    lenient()
        .when(mecanicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(atendenteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient().when(mecanicoService.getByCpf(anyString())).thenReturn(Optional.empty());
    lenient().when(atendenteService.getByCpf(anyString())).thenReturn(Optional.empty());

    mockMecanico = mock(Mecanico.class);
    lenient().when(mockMecanico.getId()).thenReturn(UUID.randomUUID());
    lenient().when(mockMecanico.getNome()).thenReturn("Mecanico Mock");
    lenient().when(mecanicoFactory.createMany(anyInt())).thenReturn(List.of(mockMecanico));
    lenient().when(mecanicoService.create(any(Mecanico.class))).thenAnswer(i -> i.getArgument(0));
    lenient()
        .when(mecanicoService.update(any(), any(Mecanico.class)))
        .thenAnswer(i -> i.getArgument(1));

    mockAtendente = mock(Atendente.class);
    lenient().when(mockAtendente.getId()).thenReturn(UUID.randomUUID());
    lenient().when(mockAtendente.getNome()).thenReturn("Atendente Mock");
    lenient().when(atendenteFactory.createMany(anyInt())).thenReturn(List.of(mockAtendente));
    lenient().when(atendenteService.create(any(Atendente.class))).thenAnswer(i -> i.getArgument(0));
    lenient()
        .when(atendenteService.update(any(), any(Atendente.class)))
        .thenAnswer(i -> i.getArgument(1));

    lenient().when(adminService.create(any(Admin.class))).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  @DisplayName("Should create fixed admin, seed mecanicos and atendentes when DB is empty")
  void shouldSeedAllWhenEmpty() {
    usuarioSeeder.seed();

    verify(adminService).create(any(Admin.class));
    verify(mecanicoFactory).createMany(10);
    verify(mecanicoService, atLeastOnce()).create(any(Mecanico.class));
    verify(atendenteFactory).createMany(5);
    verify(atendenteService, atLeastOnce()).create(any(Atendente.class));
  }

  @Test
  @DisplayName("Should skip mecanico batch seeding when enough records exist")
  void shouldSkipMecanicoSeedingWhenEnoughExist() {
    when(mecanicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(10, mockMecanico)));

    usuarioSeeder.seed();

    verify(mecanicoFactory, never()).createMany(anyInt());
  }

  @Test
  @DisplayName("Should skip atendente batch seeding when enough records exist")
  void shouldSkipAtendenteSeedingWhenEnoughExist() {
    when(atendenteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(5, mockAtendente)));

    usuarioSeeder.seed();

    verify(atendenteFactory, never()).createMany(anyInt());
  }

  @Test
  @DisplayName("Should create fixed mecanico when CPF not found")
  void shouldCreateFixedMecanicoWhenNotExists() {
    when(mecanicoService.getByCpf(SeedingConstants.FIXED_MECANICO_CPF))
        .thenReturn(Optional.empty());

    Mecanico fixedMecanico = mock(Mecanico.class);
    when(fixedMecanico.getId()).thenReturn(UUID.randomUUID());
    // Both random batch AND fixed mecanico will call create
    when(mecanicoService.create(any(Mecanico.class))).thenReturn(fixedMecanico);
    when(mecanicoService.update(any(), any())).thenReturn(fixedMecanico);

    usuarioSeeder.seed();

    // Random batch (1 mock) + fixed mecanico = at least 2 creates
    verify(mecanicoService, atLeast(2)).create(any(Mecanico.class));
    verify(mecanicoService).update(any(), any(Mecanico.class));
  }

  @Test
  @DisplayName("Should skip fixed mecanico creation when CPF already exists")
  void shouldSkipFixedMecanicoWhenAlreadyExists() {
    Mecanico existing = mock(Mecanico.class);
    when(mecanicoService.getByCpf(SeedingConstants.FIXED_MECANICO_CPF))
        .thenReturn(Optional.of(existing));

    usuarioSeeder.seed();

    // update should never be called — fixed mecanico was not created
    verify(mecanicoService, never()).update(any(), any(Mecanico.class));
  }

  @Test
  @DisplayName("Should create fixed atendente when CPF not found")
  void shouldCreateFixedAtendenteWhenNotExists() {
    when(atendenteService.getByCpf(SeedingConstants.FIXED_ATENDENTE_CPF))
        .thenReturn(Optional.empty());

    Atendente fixedAtendente = mock(Atendente.class);
    when(fixedAtendente.getId()).thenReturn(UUID.randomUUID());
    when(atendenteService.create(any(Atendente.class))).thenReturn(fixedAtendente);
    when(atendenteService.update(any(), any())).thenReturn(fixedAtendente);

    usuarioSeeder.seed();

    // Random batch (1 mock) + fixed atendente = at least 2 creates
    verify(atendenteService, atLeast(2)).create(any(Atendente.class));
    verify(atendenteService).update(any(), any(Atendente.class));
  }

  @Test
  @DisplayName("Should skip fixed atendente creation when CPF already exists")
  void shouldSkipFixedAtendenteWhenAlreadyExists() {
    Atendente existing = mock(Atendente.class);
    when(atendenteService.getByCpf(SeedingConstants.FIXED_ATENDENTE_CPF))
        .thenReturn(Optional.of(existing));

    usuarioSeeder.seed();

    verify(atendenteService, never()).update(any(), any(Atendente.class));
  }

  @Test
  @DisplayName("Should handle admin creation failure gracefully and continue seeding")
  void shouldHandleAdminCreationFailureGracefully() {
    when(adminService.create(any(Admin.class))).thenThrow(new RuntimeException("Duplicate email"));

    // Should NOT propagate exception
    usuarioSeeder.seed();

    // Mecanico and atendente seedings still run
    verify(mecanicoFactory).createMany(10);
    verify(atendenteFactory).createMany(5);
  }

  @Test
  @DisplayName("Should handle individual mecanico creation failure gracefully")
  void shouldHandleMecanicoCreationFailureGracefully() {
    when(mecanicoService.create(any(Mecanico.class)))
        .thenThrow(new RuntimeException("Constraint violation"));

    // Should NOT propagate exception
    usuarioSeeder.seed();
  }

  @Test
  @DisplayName("Should seed mecanicos using the count threshold of 10")
  void shouldSeedMecanicosUpToThreshold() {
    when(mecanicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(9, mockMecanico)));

    usuarioSeeder.seed();

    verify(mecanicoFactory).createMany(10);
  }

  @Test
  @DisplayName("Should seed atendentes using the count threshold of 5")
  void shouldSeedAtendentesUpToThreshold() {
    when(atendenteService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(4, mockAtendente)));

    usuarioSeeder.seed();

    verify(atendenteFactory).createMany(5);
  }
}
