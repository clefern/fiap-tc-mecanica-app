package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.model.OrdemServico;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoFactory extends SeederFactory<OrdemServico> {

  private static final Logger log = LoggerFactory.getLogger(OrdemServicoFactory.class);

  private final AtomicInteger sequence = new AtomicInteger(0);

  @Override
  public OrdemServico create() {
    // Basic creation without dependencies (Client/Vehicle).
    // Should be enriched by the caller or a specialized method.
    return OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
  }

  public OrdemServico createFor(UUID clienteId, UUID veiculoId) {
    OrdemServico os = OrdemServico.nova(clienteId, veiculoId);

    // Append sequence to avoid collision during rapid seeding
    os.setCodigo(os.getCodigo() + "-" + sequence.getAndIncrement());

    os.setObservacoes(faker.lorem().sentence());
    // Retorna ABERTA para permitir adição de itens no SeedingService
    return os;
  }
}
