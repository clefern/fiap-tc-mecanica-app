package com.fiap.mecanica.infra.seeding;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Orchestrator for database seeding. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
  private static final int MAX_RETRIES = 3;
  private static final long BACKOFF_MILLIS = 2000L;

  private final AsyncSeedingExecutor asyncSeedingExecutor;

  @Value("${seeding.enabled:true}")
  private boolean seedingEnabled;

  @Override
  public void run(String... args) {
    if (!seedingEnabled) {
      log.info("⚠️ Seeding is disabled by configuration.");
      return;
    }

    final var retry = createRetry();
    retry
        .getEventPublisher()
        .onRetry(
            event ->
                log.warn(
                    "⚠️ Database Seeding failed (Attempt {}/{}). Retrying in {}ms.",
                    event.getNumberOfRetryAttempts(),
                    MAX_RETRIES,
                    BACKOFF_MILLIS));

    try {
      Retry.decorateRunnable(retry, this::runSeedAsyncAndWait).run();
      log.info("✅ Database Seeding Completed Successfully.");
    } catch (Exception exception) {
      log.error("❌ Database Seeding failed after {} attempts.", MAX_RETRIES, exception);
    }
  }

  private Retry createRetry() {
    final var retryConfig =
        RetryConfig.custom()
            .maxAttempts(MAX_RETRIES)
            .waitDuration(Duration.ofMillis(BACKOFF_MILLIS))
            .retryExceptions(Exception.class)
            .build();

    return Retry.of("database-seeder", retryConfig);
  }

  private void runSeedAsyncAndWait() {
    log.info("🌱 Starting Database Seeding attempt.");

    try {
      asyncSeedingExecutor.seedAsync().get();
    } catch (InterruptedException e) {
      if (!Thread.currentThread().isInterrupted()) {
        Thread.currentThread().interrupt();
      }

      throw new IllegalStateException(
          "Database Seeding interrupted while waiting async completion.", e);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Unhandled exception seeding data into datasource", e);
    }
  }
}
