package com.fiap.mecanica.infra.seeding.seeder;

import com.fiap.mecanica.application.service.AdminService;
import com.fiap.mecanica.application.service.AtendenteService;
import com.fiap.mecanica.application.service.MecanicoService;
import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.seeding.Seeder;
import com.fiap.mecanica.infra.seeding.SeedingConstants;
import com.fiap.mecanica.infra.seeding.factory.AtendenteFactory;
import com.fiap.mecanica.infra.seeding.factory.MecanicoFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Seeds all users: fixed admin, fixed mecanico, fixed atendente, and random batches of each. */
@Component
@RequiredArgsConstructor
public class UsuarioSeeder implements Seeder {

  private static final Logger logger = LoggerFactory.getLogger(UsuarioSeeder.class);

  private final MecanicoFactory mecanicoFactory;
  private final AtendenteFactory atendenteFactory;
  private final MecanicoService mecanicoService;
  private final AtendenteService atendenteService;
  private final AdminService adminService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void seed() {
    String passwordHash = passwordEncoder.encode(SeedingConstants.DEFAULT_PASSWORD);
    createFixedAdminUser(passwordHash);
    seedMecanicos(10, passwordHash);
    seedAtendentes(5, passwordHash);
  }

  private void createFixedAdminUser(String passwordHash) {
    String email = SeedingConstants.FIXED_ADMIN_EMAIL;
    try {
      Admin admin = new Admin("Admin User", Email.of(email), passwordHash);
      var saved = adminService.create(admin);
      logger.info("✅ Fixed Admin User created: {} / 123456 (ID: {})", email, saved.getId());
    } catch (Exception e) {
      logger.info("Admin user creation skipped (likely exists): {}", e.getMessage());
    }
  }

  private void seedMecanicos(int count, String passwordHash) {
    long currentCount = mecanicoService.getAll(Pageable.unpaged()).getTotalElements();
    if (currentCount >= count) {
      logger.info("Skipping Mecanico seeding, already have {} records.", currentCount);
      return;
    }

    logger.info("Seeding {} Mecanicos...", count);
    List<Mecanico> mecanicos = mecanicoFactory.createMany(count);
    for (Mecanico m : mecanicos) {
      try {
        var saved = mecanicoService.create(m);
        logger.debug("Seeded Mecanico: {}", saved.getId());
      } catch (Exception e) {
        logger.warn("❌ Failed to seed mecanico {}: {}", m.getNome(), e.getMessage());
      }
    }

    createFixedMecanico(passwordHash);
  }

  private void createFixedMecanico(String passwordHash) {
    CPF cpf = CPF.of(SeedingConstants.FIXED_MECANICO_CPF);
    mecanicoService
        .getByCpf(SeedingConstants.FIXED_MECANICO_CPF)
        .orElseGet(
            () -> {
              try {
                Mecanico m =
                    new Mecanico(
                        "Mecanico Teste",
                        cpf,
                        Email.of(SeedingConstants.FIXED_MECANICO_EMAIL),
                        "Mecânica Geral");
                var saved = mecanicoService.create(m);
                saved.setPassword(passwordHash);
                saved = mecanicoService.update(saved.getId(), saved);
                logger.info("✅ Fixed Mecanico created: {}", SeedingConstants.FIXED_MECANICO_EMAIL);
                return saved;
              } catch (Exception e) {
                logger.error("❌ Failed to create fixed mecanico: {}", e.getMessage());
                return null;
              }
            });
  }

  private void seedAtendentes(int count, String passwordHash) {
    long currentCount = atendenteService.getAll(Pageable.unpaged()).getTotalElements();
    if (currentCount >= count) {
      logger.info("Skipping Atendente seeding, already have {} records.", currentCount);
      return;
    }

    logger.info("Seeding {} Atendentes...", count);
    List<Atendente> atendentes = atendenteFactory.createMany(count);
    for (Atendente a : atendentes) {
      try {
        var saved = atendenteService.create(a);
        logger.debug("Seeded Atendente: {}", saved.getId());
      } catch (Exception e) {
        logger.warn("❌ Failed to seed atendente {}: {}", a.getNome(), e.getMessage());
      }
    }

    createFixedAtendente(passwordHash);
  }

  private void createFixedAtendente(String passwordHash) {
    CPF cpf = CPF.of(SeedingConstants.FIXED_ATENDENTE_CPF);
    atendenteService
        .getByCpf(SeedingConstants.FIXED_ATENDENTE_CPF)
        .orElseGet(
            () -> {
              try {
                Atendente a =
                    new Atendente(
                        "Atendente Teste", cpf, Email.of(SeedingConstants.FIXED_ATENDENTE_EMAIL));
                var saved = atendenteService.create(a);
                saved.setPassword(passwordHash);
                saved = atendenteService.update(saved.getId(), saved);
                logger.info(
                    "✅ Fixed Atendente created: {}", SeedingConstants.FIXED_ATENDENTE_EMAIL);
                return saved;
              } catch (Exception e) {
                logger.error("❌ Failed to create fixed atendente: {}", e.getMessage());
                return null;
              }
            });
  }
}
