package com.fiap.mecanica.infra.seeding;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsyncSeedingExecutor {
  private final SeedingOrchestrator orchestrator;

  @Async
  public CompletableFuture<Void> seedAsync() {
    orchestrator.seed();
    return CompletableFuture.completedFuture(null);
  }
}
