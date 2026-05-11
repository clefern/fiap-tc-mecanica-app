package com.fiap.mecanica.infra.seeding.seeder;

import com.fiap.mecanica.application.service.ClienteService;
import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.application.service.MecanicoService;
import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.application.service.OrdemServicoService;
import com.fiap.mecanica.application.service.OsItemService;
import com.fiap.mecanica.application.service.OsLifecycleService;
import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.application.service.ServicoService;
import com.fiap.mecanica.application.service.VeiculoService;
import com.fiap.mecanica.application.service.prioridade.PrioridadeService;
import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.exception.ItemDuplicadoException;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.infra.seeding.Seeder;
import com.fiap.mecanica.infra.seeding.SeedingConstants;
import com.fiap.mecanica.infra.seeding.SeedingShutdownGuard;
import com.fiap.mecanica.infra.seeding.factory.ItemOrdemServicoFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds ordens de serviço: fixed demo scenarios (for presentation) and random OS across all
 * clients/vehicles.
 */
@Component
@RequiredArgsConstructor
public class OrdemServicoSeeder implements Seeder {

  private static final Logger logger = LoggerFactory.getLogger(OrdemServicoSeeder.class);

  private final Faker faker = new Faker(Locale.of("pt-BR"));

  private final SeedingShutdownGuard shutdownGuard;
  private final ItemOrdemServicoFactory itemOrdemServicoFactory;
  private final OrdemServicoService ordemServicoService;
  private final OsLifecycleService lifecycleService;
  private final OsItemService itemService;
  private final OrcamentoService orcamentoService;
  private final PrioridadeService prioridadeService;
  private final ClienteService clienteService;
  private final MecanicoService mecanicoService;
  private final VeiculoService veiculoService;
  private final ServicoService servicoService;
  private final PecaService pecaService;
  private final InsumoService insumoService;

  @Override
  public void seed() {
    if (shutdownGuard.isShuttingDown()) {
      return;
    }
    seedFixedScenarios();
    seedOrdemServicos();
  }

  // ---------------------------------------------------------------------------
  // Fixed Demo Scenarios (for presentation)
  // ---------------------------------------------------------------------------

  private void seedFixedScenarios() {
    logger.info("🧪 Seeding Fixed Test Scenarios...");
    try {
      Optional<Mecanico> mecanicoOpt =
          mecanicoService.getByCpf(SeedingConstants.FIXED_MECANICO_CPF);
      Optional<Cliente> clienteOpt =
          clienteService.getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF);
      Optional<Veiculo> veiculoOpt =
          veiculoService.getByPlaca(SeedingConstants.FIXED_VEICULO_PLACA);

      if (mecanicoOpt.isEmpty() || clienteOpt.isEmpty() || veiculoOpt.isEmpty()) {
        logger.warn(
            "❌ Fixed scenarios skipped: missing fixed mecanico, cliente or veiculo."
                + " Run UsuarioSeeder and ClienteSeeder first.");
        return;
      }

      seedFixedOSs(clienteOpt.get(), mecanicoOpt.get(), veiculoOpt.get());
      logger.info("✅ Fixed Test Scenarios Created!");
    } catch (Exception e) {
      logger.error("❌ Failed to seed fixed scenarios: {}", e.getMessage(), e);
    }
  }

  private void seedFixedOSs(Cliente cliente, Mecanico mecanico, Veiculo veiculo) {
    try {
      List<Servico> servicos = servicoService.getAll(Pageable.unpaged()).getContent();
      List<Peca> pecas = pecaService.getAll(Pageable.unpaged()).getContent();
      List<Insumo> insumos = insumoService.getAll(Pageable.unpaged()).getContent();

      if (servicos.isEmpty()) {
        logger.error("❌ Serviços está vazio para seedFixedOSs");
      }
      if (pecas.isEmpty()) {
        logger.error("❌ Peças está vazio para seedFixedOSs");
      }
      if (insumos.isEmpty()) {
        logger.error("❌ Insumos está vazio para seedFixedOSs");
      }

      createFixedOS(
          cliente, veiculo, mecanico, pecas, servicos, insumos, StatusOS.AGUARDANDO_APROVACAO);
      createFixedOS(cliente, veiculo, mecanico, pecas, servicos, insumos, StatusOS.EM_EXECUCAO);
      createFixedOS(cliente, veiculo, mecanico, pecas, servicos, insumos, StatusOS.FINALIZADA);

      logger.info("✅ Fixed OS created");
    } catch (Exception e) {
      logger.error("❌ Failed to seed fixed OS: {}", e.getMessage());
    }
  }

  private void createFixedOS(
      Cliente cliente,
      Veiculo veiculo,
      Mecanico mecanico,
      List<Peca> pecas,
      List<Servico> servicos,
      List<Insumo> insumos,
      StatusOS targetStatus) {
    try {
      OrdemServico os =
          ordemServicoService.criarOrdemServico(
              cliente.getId(), veiculo.getId(), "OS de Teste - " + targetStatus);

      adicionarItemIgnorandoDuplicata(
          os.getId(),
          itemOrdemServicoFactory.createRandom(servicos, pecas, insumos),
          mecanico.getId());
      adicionarItemIgnorandoDuplicata(
          os.getId(),
          itemOrdemServicoFactory.createRandom(servicos, pecas, insumos),
          mecanico.getId());

      transitionToStatus(os.getId(), targetStatus, mecanico.getId());

      logger.info("✅ Fixed OS created: {}", os.getCodigo());
    } catch (Exception e) {
      logger.error("❌ Failed to seed fixed OS: {}", e.getMessage());
    }
  }

  // ---------------------------------------------------------------------------
  // Random OS seeding
  // ---------------------------------------------------------------------------

  @Transactional
  public void seedOrdemServicos() {
    try {
      long currentOsCount =
          ordemServicoService.listarTodas(null, null, Pageable.unpaged()).getTotalElements();
      if (currentOsCount > 30) {
        logger.info("Skipping OrdemServico seeding, already have {} records.", currentOsCount);
        return;
      }

      List<Cliente> clientes = clienteService.getAll(Pageable.unpaged()).getContent();
      if (clientes.isEmpty()) {
        logger.warn("❌ Skipping OS seeding: missing clients or mechanics");
        return;
      }
      logger.info("Seeding random OrdemServicos...");

      List<Peca> pecas = pecaService.getAll(Pageable.unpaged()).getContent();
      List<Servico> servicos = servicoService.getAll(Pageable.unpaged()).getContent();
      List<Insumo> insumos = insumoService.getAll(Pageable.unpaged()).getContent();
      List<Mecanico> mecanicos = mecanicoService.getAll(Pageable.unpaged()).getContent();

      if (pecas.isEmpty() || servicos.isEmpty() || insumos.isEmpty()) {
        logger.warn("❌ Skipping OS seeding. Missing catalog items (Pecas, Servicos, or Insumos).");
      }

      if (mecanicos.isEmpty()) {
        logger.warn("❌ Skipping OS seeding: missing clients or mechanics");
        return;
      }

      for (Cliente cliente : clientes) {
        if (shutdownGuard.isShuttingDown()) {
          logger.warn("⚠️ Application shutting down, aborting OS seeding.");
          return;
        }

        List<Veiculo> veiculos = veiculoService.listByClienteId(cliente.getId());

        for (Veiculo v : veiculos) {
          if (shutdownGuard.isShuttingDown()) {
            logger.warn("⚠️ Application shutting down, aborting OS seeding.");
            return;
          }
          try {
            if (v.getId() == null) {
              logger.warn("❌ Vehicle {} has no ID, skipping OS creation", v.getPlaca());
              continue;
            }
            int osCount = ThreadLocalRandom.current().nextInt(1, 3); // 1 to 2

            for (int i = 0; i < osCount; i++) {
              createAndSaveOS(cliente.getId(), v.getId(), pecas, servicos, insumos, mecanicos);
            }
          } catch (Exception e) {
            logger.error("❌ Failed to seed OS for vehicle {}: {}", v.getPlaca(), e.getMessage());
          }
        }
      }
    } catch (Exception e) {
      logger.error("Critical error during OS seeding: {}", e.getMessage(), e);
      // Catch so we don't rollback previous seedings
    }
  }

  private void createAndSaveOS(
      UUID clienteId,
      UUID veiculoId,
      List<Peca> pecas,
      List<Servico> servicos,
      List<Insumo> insumos,
      List<Mecanico> mecanicos) {
    UUID osId = null;
    try {
      OrdemServico os =
          ordemServicoService.criarOrdemServico(clienteId, veiculoId, faker.lorem().sentence());
      osId = os.getId();

      Prioridade randomPriority = pickRandomPriority();
      if (randomPriority != Prioridade.NORMAL) {
        prioridadeService.atualizarPrioridade(osId, randomPriority);
      }

      UUID mecanicoId = null;
      if (mecanicos != null && !mecanicos.isEmpty()) {
        Mecanico m = mecanicos.get(ThreadLocalRandom.current().nextInt(mecanicos.size()));
        mecanicoId = m.getId();
      }

      int qtdItens = ThreadLocalRandom.current().nextInt(1, 6);
      for (int j = 0; j < qtdItens; j++) {
        var item = itemOrdemServicoFactory.createRandom(servicos, pecas, insumos);
        if (item != null) {
          adicionarItemIgnorandoDuplicata(osId, item, mecanicoId);
        }
      }

      StatusOS targetStatus = pickRandomStatus();

      try {
        transitionToStatus(osId, targetStatus, mecanicoId);
      } catch (Exception e) {
        logger.error("❌ Error saving OS for vehicle {}: {}", veiculoId, e.getMessage(), e);
        if (osId != null) {
          safeCancelOs(osId);
        }
        return;
      }

      logger.debug(
          "✅ OS created for vehicle {} (ID: {}, Status: {})", veiculoId, osId, targetStatus);
    } catch (Exception e) {
      logger.error("❌ Error saving OS for vehicle {}: {}", veiculoId, e.getMessage(), e);
      if (osId != null) {
        safeCancelOs(osId);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Status transition engine
  // ---------------------------------------------------------------------------

  private void transitionToStatus(UUID osId, StatusOS target, UUID mecanicoId) {
    if (mecanicoId == null) {
      throw new IllegalArgumentException(
          "Mechanic ID cannot be null for status transition. OS: " + osId);
    }

    logger.info("🚀 Starting transition for OS {} to target {}", osId, target);

    if (target == StatusOS.RECEBIDA) {
      logger.info("✅ Target is RECEBIDA, no transition needed for OS {}", osId);
      return;
    }

    if (target == StatusOS.CANCELADA) {
      logger.info("⚠️ Cancelling OS {}", osId);
      lifecycleService.cancelar(osId);
      return;
    }

    try {
      // RECEBIDA -> EM_DIAGNOSTICO
      logger.info("🔧 Attempting: RECEBIDA -> EM_DIAGNOSTICO for OS {}", osId);
      lifecycleService.iniciarDiagnostico(osId, mecanicoId);

      if (target == StatusOS.EM_DIAGNOSTICO) {
        return;
      }

      // EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO
      logger.info("🔧 Attempting: EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO for OS {}", osId);
      lifecycleService.finalizarDiagnostico(osId, mecanicoId);

      if (target == StatusOS.AGUARDANDO_APROVACAO) {
        return;
      }

      logger.info("💰 Attempting: Approving Budget for OS {}", osId);
      var orcamentoOpt = orcamentoService.buscarPorOrdemServico(osId);

      boolean needsApproval =
          target == StatusOS.APROVADA
              || target == StatusOS.EM_EXECUCAO
              || target == StatusOS.FINALIZADA
              || target == StatusOS.ENTREGUE;

      if (orcamentoOpt.isEmpty()) {
        if (needsApproval) {
          logger.info(
              "⚠️ No budget found for OS {} but target is {}. Generating budget manually.",
              osId,
              target);
          var osForBudget = ordemServicoService.buscarPorId(osId);
          try {
            var newOrcamento = orcamentoService.gerarOrcamento(osForBudget);
            orcamentoOpt = Optional.of(newOrcamento);
          } catch (IllegalStateException e) {
            // Concurrency handling: budget may have been created by listener in the meantime
            logger.warn(
                "⚠️ Budget generation failed (likely race condition), attempting to fetch again:"
                    + " {}",
                e.getMessage());
            orcamentoOpt = orcamentoService.buscarPorOrdemServico(osId);
            if (orcamentoOpt.isEmpty()) {
              throw new IllegalStateException(
                  "Failed to generate or fetch budget for OS " + osId, e);
            }
          }
        } else {
          logger.warn(
              "⚠️ No budget found for OS {} during seeding. Keeping status at {} and "
                  + "skipping execution/finalization transitions.",
              osId,
              StatusOS.AGUARDANDO_APROVACAO);
          return;
        }
      }

      orcamentoService.aprovar(orcamentoOpt.get().getId());

      OrdemServico osAtual = ordemServicoService.buscarPorId(osId);

      if (osAtual.getStatus() != StatusOS.APROVADA && needsApproval) {
        logger.warn(
            "⚠️ OS {} still in {} after budget approval. Forcing status to APROVADA for seeding"
                + " purposes.",
            osId,
            osAtual.getStatus());
        osAtual = lifecycleService.aprovarOS(osId);
      }

      if (osAtual.getStatus() != StatusOS.APROVADA) {
        logger.warn(
            "⚠️ After budget approval, OS {} is in status {} (expected APROVADA). "
                + "Skipping execution/finalization transitions to avoid invalid state transitions.",
            osId,
            osAtual.getStatus());
        return;
      }

      if (target == StatusOS.APROVADA) {
        return;
      }

      // APROVADA -> EM_EXECUCAO
      logger.info("🔧 Attempting: APROVADA -> EM_EXECUCAO for OS {}", osId);
      lifecycleService.iniciarExecucao(osId, mecanicoId);

      if (target == StatusOS.EM_EXECUCAO) {
        return;
      }

      // EM_EXECUCAO -> FINALIZADA
      logger.info("🏁 Attempting: EM_EXECUCAO -> FINALIZADA for OS {}", osId);
      lifecycleService.finalizar(osId, mecanicoId);

      if (target == StatusOS.FINALIZADA) {
        return;
      }

      // FINALIZADA -> ENTREGUE
      logger.info("🚗 Attempting: FINALIZADA -> ENTREGUE for OS {}", osId);
      lifecycleService.entregar(osId);

    } catch (Exception e) {
      logger.error(
          "❌ Error transitioning OS {} to {}: {}. Cancelling to unblock queue.",
          osId,
          target,
          e.getMessage());
      try {
        lifecycleService.cancelar(osId);
        logger.info("⚠️ OS {} cancelled successfully.", osId);
      } catch (Exception ex) {
        logger.error("💀 Failed to cancel OS {}: {}", osId, ex.getMessage());
      }
      throw e;
    }
  }

  private void safeCancelOs(UUID osId) {
    try {
      lifecycleService.cancelar(osId);
      logger.info("⚠️ OS {} cancelled after seeding error.", osId);
    } catch (Exception ex) {
      logger.error("💀 Failed to cancel OS {} after seeding error: {}", osId, ex.getMessage(), ex);
    }
  }

  private void adicionarItemIgnorandoDuplicata(UUID osId, ItemOrdemServico item, UUID mecanicoId) {
    try {
      itemService.adicionarItem(osId, item, mecanicoId);
    } catch (ItemDuplicadoException e) {
      logger.warn("⚠️ Item duplicado ignorado durante seeding (OS={}): {}", osId, e.getMessage());
    }
  }

  private StatusOS pickRandomStatus() {
    List<StatusOS> candidates = new ArrayList<>();
    for (StatusOS status : StatusOS.values()) {
      if (status != StatusOS.RECEBIDA && status != StatusOS.APROVADA) {
        candidates.add(status);
      }
    }
    if (candidates.isEmpty()) {
      return StatusOS.EM_DIAGNOSTICO;
    }
    return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
  }

  private Prioridade pickRandomPriority() {
    // 70% Normal, 15% Baixa, 15% Alta
    double chance = ThreadLocalRandom.current().nextDouble();
    if (chance < 0.70) {
      return Prioridade.NORMAL;
    }
    if (chance < 0.85) {
      return Prioridade.BAIXA;
    }
    return Prioridade.ALTA;
  }
}
