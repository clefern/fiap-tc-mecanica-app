package com.fiap.mecanica.infra.seeding.seeder;

import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.application.service.ServicoService;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.infra.seeding.Seeder;
import com.fiap.mecanica.infra.seeding.factory.InsumoFactory;
import com.fiap.mecanica.infra.seeding.factory.PecaFactory;
import com.fiap.mecanica.infra.seeding.factory.ServicoFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** Seeds catalog master data: peças, insumos and serviços. */
@Component
@RequiredArgsConstructor
public class CatalogoSeeder implements Seeder {

  private static final Logger logger = LoggerFactory.getLogger(CatalogoSeeder.class);

  private final PecaFactory pecaFactory;
  private final InsumoFactory insumoFactory;
  private final ServicoFactory servicoFactory;
  private final PecaService pecaService;
  private final InsumoService insumoService;
  private final ServicoService servicoService;

  @Override
  public void seed() {
    seedPecas(50);
    seedInsumos(30);
    seedServicos(20);
  }

  private void seedPecas(int count) {
    long currentCount = pecaService.getAll(Pageable.unpaged()).getTotalElements();
    if (currentCount >= count) {
      logger.info("Skipping Peca seeding, already have {} records.", currentCount);
      return;
    }

    logger.info("Seeding {} Peças...", count);
    List<Peca> pecas = pecaFactory.createMany(count);
    for (Peca p : pecas) {
      try {
        var saved = pecaService.create(p);
        logger.debug("Seeded Peca: {}", saved.getId());
      } catch (Exception e) {
        logger.warn("❌ Failed to seed peca {}: {}", p.getNome(), e.getMessage());
      }
    }
  }

  private void seedInsumos(int count) {
    long currentCount = insumoService.getAll(Pageable.unpaged()).getTotalElements();
    if (currentCount >= count) {
      logger.info("Skipping Insumo seeding, already have {} records.", currentCount);
      return;
    }

    logger.info("Seeding {} Insumos...", count);
    List<Insumo> insumos = new ArrayList<>(insumoFactory.createAllTemplates());

    // Ensure we meet the count if templates are fewer (though templates are exactly 30)
    if (insumos.size() < count) {
      int needed = count - insumos.size();
      insumos.addAll(insumoFactory.createMany(needed));
    } else if (insumos.size() > count) {
      insumos = insumos.subList(0, count);
    }

    for (Insumo i : insumos) {
      try {
        var saved = insumoService.create(i);
        logger.debug("Seeded Insumo: {}", saved.getId());
      } catch (Exception e) {
        logger.warn("❌ Failed to seed insumo {}: {}", i.getNome(), e.getMessage());
      }
    }
  }

  private void seedServicos(int count) {
    long currentCount = servicoService.getAll(Pageable.unpaged()).getTotalElements();
    if (currentCount >= count) {
      logger.info("Skipping Servico seeding, already have {} records.", currentCount);
      return;
    }

    logger.info("Seeding {} Serviços...", count);
    List<Servico> servicos = servicoFactory.createMany(count);
    for (Servico s : servicos) {
      try {
        var saved = servicoService.create(s);
        logger.debug("Seeded Servico: {}", saved.getId());
      } catch (Exception e) {
        logger.warn("❌ Failed to seed servico {}: {}", s.getNome(), e.getMessage());
      }
    }
  }
}
