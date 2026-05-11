package com.fiap.mecanica.infra.seeding;

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
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.infra.seeding.seeder.CatalogoSeeder;
import com.fiap.mecanica.infra.seeding.seeder.ClienteSeeder;
import com.fiap.mecanica.infra.seeding.seeder.OrdemServicoSeeder;
import com.fiap.mecanica.infra.seeding.seeder.UsuarioSeeder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Coordinates all seeders in the correct order and prints the final verification summary. */
@Service
@RequiredArgsConstructor
public class SeedingOrchestrator {

  private static final Logger logger = LoggerFactory.getLogger(SeedingOrchestrator.class);

  private final SeedingShutdownGuard shutdownGuard;
  private final UsuarioSeeder usuarioSeeder;
  private final CatalogoSeeder catalogoSeeder;
  private final ClienteSeeder clienteSeeder;
  private final OrdemServicoSeeder ordemServicoSeeder;

  private final ClienteService clienteService;
  private final VeiculoService veiculoService;
  private final MecanicoService mecanicoService;
  private final AtendenteService atendenteService;
  private final ServicoService servicoService;
  private final PecaService pecaService;
  private final InsumoService insumoService;
  private final OrdemServicoService ordemServicoService;

  public void seed() {
    logger.info("[SEED_START] 🌱 Iniciando seeding do banco de dados...");

    runIfActive("UsuarioSeeder", usuarioSeeder::seed);
    runIfActive("CatalogoSeeder", catalogoSeeder::seed);
    runIfActive("ClienteSeeder", clienteSeeder::seed);
    runIfActive("OrdemServicoSeeder", ordemServicoSeeder::seed);

    if (shutdownGuard.isShuttingDown()) {
      logger.warn("[SEED_END] ⚠️ Seeding abortado — aplicação em shutdown.");
      return;
    }
    logger.info("[SEED_END] ✅ Seeding do banco de dados concluído!");
    verifySeeding();
  }

  private void runIfActive(String name, Runnable seeder) {
    if (shutdownGuard.isShuttingDown()) {
      logger.warn("[SEED_SKIP] ⚠️ Pulando {} — aplicação em shutdown.", name);
      return;
    }
    seeder.run();
  }

  private void verifySeeding() {
    if (shutdownGuard.isShuttingDown()) {
      return;
    }
    try {
      logger.info("[SEED_VERIFY] 📊 Iniciando verificação detalhada do seeding...");

      List<Cliente> clientes = clienteService.getAll(Pageable.unpaged()).getContent();
      long totalClientes = clientes.size();

      logger.info("[SEED_VERIFY]   - Total Clientes: {}", totalClientes);
      logger.info(
          "[SEED_VERIFY]   - Total Veiculos: {}",
          veiculoService.getAll(Pageable.unpaged()).getTotalElements());
      logger.info(
          "[SEED_VERIFY]   - Total Mecanicos: {}",
          mecanicoService.getAll(Pageable.unpaged()).getTotalElements());
      logger.info(
          "[SEED_VERIFY]   - Total Atendentes: {}",
          atendenteService.getAll(Pageable.unpaged()).getTotalElements());
      logger.info(
          "[SEED_VERIFY]   - Total Serviços: {}",
          servicoService.getAll(Pageable.unpaged()).getTotalElements());
      logger.info(
          "[SEED_VERIFY]   - Total Peças: {}",
          pecaService.getAll(Pageable.unpaged()).getTotalElements());
      logger.info(
          "[SEED_VERIFY]   - Total Insumos: {}",
          insumoService.getAll(Pageable.unpaged()).getTotalElements());
      logger.info(
          "[SEED_VERIFY]   - Total Ordens Serviço: {}",
          ordemServicoService.listarTodas(null, null, Pageable.unpaged()).getTotalElements());

      if (totalClientes == 0) {
        return;
      }

      long countFisica = clientes.stream().filter(c -> c.getTipo() == TipoPessoa.FISICA).count();
      long countJuridica =
          clientes.stream().filter(c -> c.getTipo() == TipoPessoa.JURIDICA).count();

      logger.info("[SEED_VERIFY_DISTRIBUTION] FISICA={} JURIDICA={}", countFisica, countJuridica);

      if (Math.abs(countFisica - countJuridica) > 1) {
        logger.error("[SEED_VERIFY_DISTRIBUTION] ❌ Distribution mismatch! Esperado ~50/50.");
      } else {
        logger.info("[SEED_VERIFY_DISTRIBUTION] ✅ Distribuição OK (50/50).");
      }

      long clientsWithoutVehicles = 0;
      for (Cliente c : clientes) {
        List<Veiculo> veiculos = veiculoService.listByClienteId(c.getId());
        if (veiculos.isEmpty()) {
          clientsWithoutVehicles++;
        }
      }

      if (clientsWithoutVehicles > 0) {
        logger.error(
            "[SEED_VERIFY_VEICULOS] ❌ Encontrados {} clientes sem veículos! Associação mandatória"
                + " falhou.",
            clientsWithoutVehicles);
      } else {
        logger.info(
            "[SEED_VERIFY_VEICULOS] ✅ Associação de veículos OK (todos os clientes possuem"
                + " veículos).");
      }
    } catch (Exception e) {
      logger.error("[SEED_VERIFY] ❌ Falha ao verificar seeding: {}", e.getMessage());
    }
  }
}
