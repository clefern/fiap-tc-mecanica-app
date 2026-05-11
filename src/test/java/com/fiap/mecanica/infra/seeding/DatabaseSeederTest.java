package com.fiap.mecanica.infra.seeding;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseSeederTest {

  @Mock private AsyncSeedingExecutor asyncSeedingExecutor;

  @InjectMocks private DatabaseSeeder databaseSeeder;

  @Test
  @DisplayName("Should seed successfully")
  void shouldSeedSuccessfully() throws Exception {
    setSeedingEnabled(true);
    when(asyncSeedingExecutor.seedAsync()).thenReturn(CompletableFuture.completedFuture(null));

    databaseSeeder.run();

    verify(asyncSeedingExecutor).seedAsync();
  }

  @Test
  @DisplayName("Should retry on failure")
  void shouldRetryOnFailure() throws Exception {
    setSeedingEnabled(true);
    when(asyncSeedingExecutor.seedAsync())
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Fail 1")))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Fail 2")))
        .thenReturn(CompletableFuture.completedFuture(null));

    databaseSeeder.run();

    verify(asyncSeedingExecutor, times(3)).seedAsync();
  }

  @Test
  @DisplayName("Should give up after max retries")
  void shouldGiveUpAfterMaxRetries() throws Exception {
    setSeedingEnabled(true);
    when(asyncSeedingExecutor.seedAsync())
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Fail")));

    databaseSeeder.run();

    verify(asyncSeedingExecutor, times(3)).seedAsync();
  }

  @Test
  @DisplayName("Should skip if disabled")
  void shouldSkipIfDisabled() throws Exception {
    setSeedingEnabled(false);
    databaseSeeder.run();
    verify(asyncSeedingExecutor, never()).seedAsync();
  }

  private void setSeedingEnabled(boolean enabled) throws Exception {
    Field field = DatabaseSeeder.class.getDeclaredField("seedingEnabled");
    field.setAccessible(true);
    field.set(databaseSeeder, enabled);
  }
}
