package com.fiap.mecanica.infra.seeding.seeder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.application.service.ServicoService;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.infra.seeding.factory.InsumoFactory;
import com.fiap.mecanica.infra.seeding.factory.PecaFactory;
import com.fiap.mecanica.infra.seeding.factory.ServicoFactory;
import java.util.Collections;
import java.util.List;
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
class CatalogoSeederTest {

  @Mock private PecaFactory pecaFactory;
  @Mock private InsumoFactory insumoFactory;
  @Mock private ServicoFactory servicoFactory;
  @Mock private PecaService pecaService;
  @Mock private InsumoService insumoService;
  @Mock private ServicoService servicoService;

  @InjectMocks private CatalogoSeeder catalogoSeeder;

  @BeforeEach
  void setUp() {
    lenient()
        .when(pecaService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(insumoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    lenient()
        .when(servicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    lenient().when(pecaFactory.createMany(anyInt())).thenReturn(List.of(mock(Peca.class)));
    lenient()
        .when(insumoFactory.createAllTemplates())
        .thenReturn(Collections.nCopies(30, mock(Insumo.class)));
    lenient().when(insumoFactory.createMany(anyInt())).thenReturn(List.of(mock(Insumo.class)));
    lenient().when(servicoFactory.createMany(anyInt())).thenReturn(List.of(mock(Servico.class)));

    lenient().when(pecaService.create(any(Peca.class))).thenAnswer(i -> i.getArgument(0));
    lenient().when(insumoService.create(any(Insumo.class))).thenAnswer(i -> i.getArgument(0));
    lenient().when(servicoService.create(any(Servico.class))).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  @DisplayName("Should seed all catalog items when DB is empty")
  void shouldSeedAllCatalogItemsWhenEmpty() {
    catalogoSeeder.seed();

    verify(pecaFactory).createMany(50);
    verify(pecaService, atLeastOnce()).create(any(Peca.class));

    verify(insumoFactory).createAllTemplates();
    verify(insumoService, atLeastOnce()).create(any(Insumo.class));

    verify(servicoFactory).createMany(20);
    verify(servicoService, atLeastOnce()).create(any(Servico.class));
  }

  @Test
  @DisplayName("Should skip peca seeding when count is at or above threshold of 50")
  void shouldSkipPecaSeedingWhenEnoughExist() {
    when(pecaService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(50, mock(Peca.class))));

    catalogoSeeder.seed();

    verify(pecaFactory, never()).createMany(anyInt());
    verify(pecaService, never()).create(any(Peca.class));
  }

  @Test
  @DisplayName("Should skip insumo seeding when count is at or above threshold of 30")
  void shouldSkipInsumoSeedingWhenEnoughExist() {
    when(insumoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(30, mock(Insumo.class))));

    catalogoSeeder.seed();

    verify(insumoFactory, never()).createAllTemplates();
    verify(insumoService, never()).create(any(Insumo.class));
  }

  @Test
  @DisplayName("Should skip servico seeding when count is at or above threshold of 20")
  void shouldSkipServicoSeedingWhenEnoughExist() {
    when(servicoService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(20, mock(Servico.class))));

    catalogoSeeder.seed();

    verify(servicoFactory, never()).createMany(anyInt());
    verify(servicoService, never()).create(any(Servico.class));
  }

  @Test
  @DisplayName(
      "Should use createAllTemplates (not createMany) for insumos when templates match count")
  void shouldUseTemplatesForInsumos() {
    catalogoSeeder.seed();

    verify(insumoFactory).createAllTemplates();
    verify(insumoFactory, never()).createMany(anyInt());
  }

  @Test
  @DisplayName("Should supplement insumos with createMany when templates return fewer than count")
  void shouldSupplementInsumosWhenTemplatesAreFewer() {
    when(insumoFactory.createAllTemplates())
        .thenReturn(Collections.nCopies(20, mock(Insumo.class)));

    catalogoSeeder.seed();

    verify(insumoFactory).createAllTemplates();
    verify(insumoFactory).createMany(10); // needs 30 - 20 = 10 extra
  }

  @Test
  @DisplayName("Should trim insumos when templates exceed the target count of 30")
  void shouldTrimInsumosWhenTemplatesExceedCount() {
    when(insumoFactory.createAllTemplates())
        .thenReturn(Collections.nCopies(40, mock(Insumo.class)));

    catalogoSeeder.seed();

    // Only 30 items should be persisted despite 40 being generated
    verify(insumoService, times(30)).create(any(Insumo.class));
  }

  @Test
  @DisplayName("Should handle individual peca creation failure gracefully")
  void shouldHandlePecaCreationFailureGracefully() {
    Peca bad = mock(Peca.class);
    when(bad.getNome()).thenReturn("BadPeca");
    when(pecaFactory.createMany(anyInt())).thenReturn(List.of(bad));
    when(pecaService.create(any(Peca.class))).thenThrow(new RuntimeException("Constraint error"));

    // Should NOT propagate
    catalogoSeeder.seed();

    // Other catalog items still seeded
    verify(insumoFactory).createAllTemplates();
    verify(servicoFactory).createMany(20);
  }

  @Test
  @DisplayName("Should handle individual insumo creation failure gracefully")
  void shouldHandleInsumoCreationFailureGracefully() {
    Insumo bad = mock(Insumo.class);
    when(bad.getNome()).thenReturn("BadInsumo");
    when(insumoFactory.createAllTemplates()).thenReturn(List.of(bad));
    when(insumoService.create(any(Insumo.class))).thenThrow(new RuntimeException("DB error"));

    catalogoSeeder.seed();

    verify(servicoFactory).createMany(20);
  }

  @Test
  @DisplayName("Should seed pecas when current count is below threshold")
  void shouldSeedPecasWhenBelowThreshold() {
    when(pecaService.getAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.nCopies(49, mock(Peca.class))));

    catalogoSeeder.seed();

    verify(pecaFactory).createMany(50);
  }
}
