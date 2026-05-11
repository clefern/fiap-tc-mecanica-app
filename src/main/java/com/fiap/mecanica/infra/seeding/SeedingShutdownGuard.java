package com.fiap.mecanica.infra.seeding;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Detects application shutdown and exposes a flag so seeders can abort gracefully instead of
 * crashing with EntityManagerFactory-closed errors.
 */
@Component
public class SeedingShutdownGuard {

  private volatile boolean shuttingDown;

  @EventListener(ContextClosedEvent.class)
  public void onShutdown() {
    shuttingDown = true;
  }

  public boolean isShuttingDown() {
    return shuttingDown;
  }
}
