package com.fiap.mecanica.infra.seeding;

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

  private final SeedingOrchestrator orchestrator;

  @Value("${seeding.enabled:true}")
  private boolean seedingEnabled;

  @Override
  public void run(String... args) {
    if (!seedingEnabled) {
      log.info("⚠️ Seeding is disabled by configuration.");
      return;
    }

    log.info("🌱 Starting Database Seeding...");
    try {
      orchestrator.seed();
      log.info("✅ Database Seeding Completed Successfully.");
    } catch (Exception exception) {
      log.error("❌ Database Seeding failed.", exception);
    }
  }
}
