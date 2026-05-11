package com.fiap.mecanica.infra.seeding.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.model.Servico;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemOrdemServicoFactoryTest {

  private final ItemOrdemServicoFactory factory = new ItemOrdemServicoFactory();

  @Test
  @DisplayName("Should return null for default create")
  void shouldReturnNullForDefaultCreate() {
    assertThat(factory.create()).isNull();
  }

  @Test
  @DisplayName("Should create item from Servico")
  void shouldCreateFromServico() {
    Servico servico =
        new Servico(
            UUID.randomUUID(),
            "Troca de Óleo",
            "Descricao",
            new BigDecimal("100.00"),
            Duration.ofHours(1),
            CategoriaServico.MANUTENCAO_PREVENTIVA);

    ItemOrdemServico item = factory.createFromServico(servico);

    assertThat(item).isNotNull();
    assertThat(item.getTipo()).isEqualTo(TipoItem.SERVICO);
    assertThat(item.getDescricao()).isEqualTo(servico.getNome());
    assertThat(item.getValorUnitario()).isEqualTo(servico.getPrecoBase());
    assertThat(item.getQuantidade()).isEqualTo(1);
    assertThat(item.getReferenciaId()).isEqualTo(servico.getId());
  }

  @Test
  @DisplayName("Should create item from Peca")
  void shouldCreateFromPeca() {
    Peca peca =
        new Peca(
            UUID.randomUUID(),
            "Filtro de Óleo",
            "Descricao",
            new BigDecimal("50.00"),
            true,
            "Fabricante",
            "COD123",
            "ModeloX",
            10,
            1,
            20);

    ItemOrdemServico item = factory.createFromPeca(peca);

    assertThat(item).isNotNull();
    assertThat(item.getTipo()).isEqualTo(TipoItem.PECA);
    assertThat(item.getDescricao()).isEqualTo(peca.getNome());
    assertThat(item.getValorUnitario()).isEqualTo(peca.getPrecoBase());
    assertThat(item.getQuantidade()).isBetween(1, 3);
    assertThat(item.getReferenciaId()).isEqualTo(peca.getId());
  }

  @Test
  @DisplayName("Should create item from Insumo")
  void shouldCreateFromInsumo() {
    Insumo insumo =
        new Insumo(
            UUID.randomUUID(),
            "Estopa",
            "Descricao",
            new BigDecimal("10.00"),
            true,
            "kg",
            100,
            10,
            200);

    ItemOrdemServico item = factory.createFromInsumo(insumo);

    assertThat(item).isNotNull();
    assertThat(item.getTipo()).isEqualTo(TipoItem.INSUMO);
    assertThat(item.getDescricao()).isEqualTo(insumo.getNome());
    assertThat(item.getValorUnitario()).isEqualTo(insumo.getPrecoBase());
    assertThat(item.getQuantidade()).isBetween(1, 5);
    assertThat(item.getReferenciaId()).isEqualTo(insumo.getId());
  }

  @Test
  @DisplayName("Should create random item from lists")
  void shouldCreateRandomItem() {
    Servico servico =
        new Servico(
            UUID.randomUUID(),
            "S",
            "D",
            BigDecimal.TEN,
            Duration.ofHours(1),
            CategoriaServico.MANUTENCAO_PREVENTIVA);

    Peca peca =
        new Peca(UUID.randomUUID(), "P", "D", BigDecimal.TEN, true, "F", "C", "M", 10, 1, 20);

    Insumo insumo = new Insumo(UUID.randomUUID(), "I", "D", BigDecimal.TEN, true, "U", 10, 1, 20);

    List<Servico> servicos = Collections.singletonList(servico);
    List<Peca> pecas = Collections.singletonList(peca);
    List<Insumo> insumos = Collections.singletonList(insumo);

    for (int i = 0; i < 50; i++) {
      ItemOrdemServico item = factory.createRandom(servicos, pecas, insumos);
      assertThat(item).isNotNull();
      // Just verify it returns something valid
      assertThat(item.getReferenciaId()).isIn(servico.getId(), peca.getId(), insumo.getId());
    }
  }

  @Test
  @DisplayName("Should return null when all lists are empty")
  void shouldReturnNullWhenAllListsEmpty() {
    List<Servico> servicos = Collections.emptyList();
    List<Peca> pecas = Collections.emptyList();
    List<Insumo> insumos = Collections.emptyList();

    ItemOrdemServico item = factory.createRandom(servicos, pecas, insumos);

    assertThat(item).isNull();
  }

  @Test
  @DisplayName("Should create item from Insumo when only insumos available")
  void shouldCreateFromInsumoWhenOnlyInsumosAvailable() {
    Insumo insumo = new Insumo(UUID.randomUUID(), "I", "D", BigDecimal.TEN, true, "U", 10, 1, 20);

    List<Servico> servicos = Collections.emptyList();
    List<Peca> pecas = Collections.emptyList();
    List<Insumo> insumos = Collections.singletonList(insumo);

    ItemOrdemServico item = factory.createRandom(servicos, pecas, insumos);

    assertThat(item).isNotNull();
    assertThat(item.getTipo()).isEqualTo(TipoItem.INSUMO);
    assertThat(item.getReferenciaId()).isEqualTo(insumo.getId());
  }
}
