package com.fiap.mecanica.infra.seeding.seeder;

import com.fiap.mecanica.application.service.ClienteService;
import com.fiap.mecanica.application.service.VeiculoService;
import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import com.fiap.mecanica.infra.seeding.Seeder;
import com.fiap.mecanica.infra.seeding.SeedingConstants;
import com.fiap.mecanica.infra.seeding.factory.ClienteFactory;
import com.fiap.mecanica.infra.seeding.factory.VeiculoFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds clientes (50% PF / 50% PJ) and their vehicles (at least one per client). */
@Component
@RequiredArgsConstructor
public class ClienteSeeder implements Seeder {

  private static final Logger logger = LoggerFactory.getLogger(ClienteSeeder.class);

  private final ClienteFactory clienteFactory;
  private final VeiculoFactory veiculoFactory;
  private final ClienteService clienteService;
  private final VeiculoService veiculoService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void seed() {
    String passwordHash = passwordEncoder.encode(SeedingConstants.DEFAULT_PASSWORD);
    seedClientes(10);
    createFixedCliente(passwordHash);
    seedVeiculos();
  }

  private void seedClientes(int count) {
    long currentCount = clienteService.getAll(Pageable.unpaged()).getTotalElements();
    if (currentCount >= count) {
      logger.info("Skipping Cliente seeding, already have {} records.", currentCount);
      return;
    }

    logger.info("Seeding {} Clientes (50% Fisica, 50% Juridica)...", count);

    int countFisica = count / 2;
    int countJuridica = count - countFisica;

    List<Cliente> clientes = new ArrayList<>();

    for (int i = 0; i < countFisica; i++) {
      try {
        clientes.add(clienteFactory.create(TipoPessoa.FISICA));
      } catch (Exception e) {
        logger.warn("❌ Skipping invalid PF client generation: {}", e.getMessage());
      }
    }
    for (int i = 0; i < countJuridica; i++) {
      try {
        clientes.add(clienteFactory.create(TipoPessoa.JURIDICA));
      } catch (Exception e) {
        logger.warn("❌ Skipping invalid PJ client generation: {}", e.getMessage());
      }
    }

    Collections.shuffle(clientes);

    for (Cliente c : clientes) {
      try {
        var saved = clienteService.create(c);
        logger.debug("Seeded Cliente: {}", saved.getId());
      } catch (Exception e) {
        logger.warn("❌ Failed to seed cliente {}: {}", c.getDocumento().valor(), e.getMessage());
      }
    }
  }

  private void createFixedCliente(String passwordHash) {
    CPF cpf = CPF.of(SeedingConstants.FIXED_CLIENTE_CPF);
    clienteService
        .getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF)
        .orElseGet(
            () -> {
              try {
                Cliente c =
                    new Cliente(
                        "Cliente Teste",
                        cpf,
                        TipoPessoa.FISICA,
                        Email.of(SeedingConstants.FIXED_CLIENTE_EMAIL),
                        TelefoneBr.of("11999999999"),
                        Endereco.of("Rua Teste, 123, Bairro Teste, Cidade Teste - SP, 01234567"));
                var saved = clienteService.create(c);
                saved.setPassword(passwordHash);
                saved = clienteService.update(saved.getId(), saved);
                logger.info("✅ Fixed Cliente created: {}", SeedingConstants.FIXED_CLIENTE_EMAIL);
                return saved;
              } catch (Exception e) {
                logger.error("❌ Failed to create fixed cliente: {}", e.getMessage());
                return null;
              }
            });
  }

  @Transactional
  public void seedVeiculos() {
    List<Cliente> clientes = clienteService.getAll(Pageable.unpaged()).getContent();

    if (clientes.isEmpty()) {
      logger.warn("❌ Skipping Veiculo seeding. No clientes found to associate.");
      return;
    }

    logger.info("Seeding Veiculos for {} clients...", clientes.size());

    int vehiclesCreated = 0;

    for (Cliente cliente : clientes) {
      try {
        long existing = veiculoService.listByClienteId(cliente.getId()).size();
        if (existing > 0) {
          continue;
        }

        // 1. Mandatory vehicle
        createAndSaveVeiculo(cliente);
        vehiclesCreated++;

        // 2. Chance for multiple (30-40%) - Target 35%
        if (shouldCreateExtraVehicles()) {
          int extras = getNumberOfExtraVehicles();
          for (int i = 0; i < extras; i++) {
            createAndSaveVeiculo(cliente);
            vehiclesCreated++;
          }
        }
      } catch (Exception e) {
        logger.error(
            "❌ Failed to seed vehicles for client {}: {}", cliente.getId(), e.getMessage(), e);
      }
    }

    ensureFixedVeiculo();

    logger.info("Finished seeding {} vehicles.", vehiclesCreated);
  }

  private void ensureFixedVeiculo() {
    String placa = SeedingConstants.FIXED_VEICULO_PLACA;
    veiculoService
        .getByPlaca(placa)
        .orElseGet(
            () ->
                clienteService
                    .getByDocumento(SeedingConstants.FIXED_CLIENTE_CPF)
                    .map(
                        cliente -> {
                          try {
                            Veiculo v =
                                new Veiculo(PlacaVeiculo.of(placa), "Toyota", "Corolla", 2020);
                            var saved = veiculoService.create(cliente.getId(), v);
                            logger.info("✅ Fixed Veiculo created: {}", placa);
                            return saved;
                          } catch (Exception e) {
                            logger.error("❌ Failed to create fixed veiculo: {}", e.getMessage());
                            return null;
                          }
                        })
                    .orElse(null));
  }

  private void createAndSaveVeiculo(Cliente cliente) {
    if (cliente.getId() != null) {
      try {
        Veiculo v = veiculoFactory.create();
        var saved = veiculoService.create(cliente.getId(), v);
        logger.debug(
            "✅ Vehicle created for client {} (ID: {})", cliente.getDocumento(), saved.getId());
      } catch (Exception e) {
        logger.error(
            "❌ Error saving vehicle for client {}: {}", cliente.getDocumento(), e.getMessage());
        throw e;
      }
    } else {
      logger.warn(
          "❌ Cannot create vehicle: Client ID is null (Document: {})", cliente.getDocumento());
    }
  }

  protected boolean shouldCreateExtraVehicles() {
    return ThreadLocalRandom.current().nextDouble() < 0.35;
  }

  protected int getNumberOfExtraVehicles() {
    return 1 + ThreadLocalRandom.current().nextInt(2);
  }
}
