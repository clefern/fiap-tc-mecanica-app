package com.fiap.mecanica.infra.seeding.factory;

import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.model.Servico;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ItemOrdemServicoFactory extends SeederFactory<ItemOrdemServico> {

  @Override
  public ItemOrdemServico create() {
    // Requires context (Service/Part/Insumo), so default create isn't very useful
    // alone
    return null;
  }

  public ItemOrdemServico createFromServico(Servico servico) {
    return ItemOrdemServico.builder()
        .id(UUID.randomUUID())
        .tipo(TipoItem.SERVICO)
        .descricao(servico.getNome())
        .valorUnitario(servico.getPrecoBase())
        .quantidade(1) // Services usually qty 1
        .referenciaId(servico.getId())
        .build();
  }

  public ItemOrdemServico createFromPeca(Peca peca) {
    return ItemOrdemServico.builder()
        .id(UUID.randomUUID())
        .tipo(TipoItem.PECA)
        .descricao(peca.getNome())
        .valorUnitario(peca.getPrecoBase())
        .quantidade(faker.number().numberBetween(1, 3))
        .referenciaId(peca.getId())
        .build();
  }

  public ItemOrdemServico createFromInsumo(Insumo insumo) {
    return ItemOrdemServico.builder()
        .id(UUID.randomUUID())
        .tipo(TipoItem.INSUMO)
        .descricao(insumo.getNome())
        .valorUnitario(insumo.getPrecoBase())
        .quantidade(faker.number().numberBetween(1, 5))
        .referenciaId(insumo.getId())
        .build();
  }

  public ItemOrdemServico createRandom(
      List<Servico> servicos, List<Peca> pecas, List<Insumo> insumos) {
    int roll = faker.number().numberBetween(1, 101);

    // 50% Service, 30% Part, 20% Insumo
    if (roll <= 50 && !servicos.isEmpty()) {
      return createFromServico(servicos.get(faker.number().numberBetween(0, servicos.size())));
    } else if (roll <= 80 && !pecas.isEmpty()) {
      return createFromPeca(pecas.get(faker.number().numberBetween(0, pecas.size())));
    } else if (!insumos.isEmpty()) {
      return createFromInsumo(insumos.get(faker.number().numberBetween(0, insumos.size())));
    }
    return null;
  }
}
