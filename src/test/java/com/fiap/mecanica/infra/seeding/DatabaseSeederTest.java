package com.fiap.mecanica.infra.seeding;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseSeederTest {

  @Mock private SeedingOrchestrator seedingOrchestrator;

  @InjectMocks private DatabaseSeeder databaseSeeder;

  @BeforeEach
  void setUp() throws Exception {
    setSeedingEnabled(true);
  }

  @Test
  @DisplayName("Should execute seeding when enabled")
  void shouldExecuteSeedingWhenEnabled() {
    databaseSeeder.run();
    verify(seedingOrchestrator, times(1)).seed();
  }

  @Test
  @DisplayName("Should skip seeding when disabled")
  void shouldSkipSeedingWhenDisabled() throws Exception {
    setSeedingEnabled(false);
    databaseSeeder.run();
    verify(seedingOrchestrator, never()).seed();
  }

  @Test
  @DisplayName("Should handle exceptions during seeding")
  void shouldHandleExceptionsDuringSeeding() {
    doThrow(new RuntimeException("Seeding Error")).when(seedingOrchestrator).seed();
    databaseSeeder.run();
    verify(seedingOrchestrator, times(1)).seed();
  }

  private void setSeedingEnabled(boolean enabled) throws Exception {
    Field field = DatabaseSeeder.class.getDeclaredField("seedingEnabled");
    field.setAccessible(true);
    field.set(databaseSeeder, enabled);
  }
}
