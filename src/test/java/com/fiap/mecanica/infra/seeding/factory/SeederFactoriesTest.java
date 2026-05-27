package com.fiap.mecanica.infra.seeding.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.model.Servico;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SeederFactoriesTest {

  private final OrdemServicoFactory osFactory = new OrdemServicoFactory();
  private final ItemOrdemServicoFactory itemFactory = new ItemOrdemServicoFactory();

  @Test
  @DisplayName("OrdemServicoFactory should create basic OS")
  void osFactoryShouldCreateBasic() {
    OrdemServico os = osFactory.create();
    // OrdemServico.nova() não atribui id — o UUID é gerado quando a OS é persistida.
    // Validamos apenas que o factory devolve uma instância não-nula.
    assertNotNull(os);
  }

  @Test
  @DisplayName("OrdemServicoFactory should create for client/vehicle and vary status")
  void osFactoryShouldCreateForClientVehicle() {
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();

    // Call multiple times to hit different status branches
    for (int i = 0; i < 50; i++) {
      OrdemServico os = osFactory.createFor(clienteId, veiculoId);
      assertNotNull(os);
      assertEquals(clienteId, os.getClienteId());
      assertEquals(veiculoId, os.getVeiculoId());
      assertNotNull(os.getStatus());
      assertNotNull(os.getObservacoes());
    }
  }

  @Test
  @DisplayName("ItemOrdemServicoFactory should create items from domain objects")
  void itemFactoryShouldCreateFromDomainObjects() {
    // 1. From Servico
    Servico servico = mock(Servico.class);
    when(servico.getId()).thenReturn(UUID.randomUUID());
    when(servico.getNome()).thenReturn("Servico Teste");
    when(servico.getPrecoBase()).thenReturn(new BigDecimal("100.00"));

    ItemOrdemServico itemServico = itemFactory.createFromServico(servico);
    assertNotNull(itemServico);
    assertEquals(TipoItem.SERVICO, itemServico.getTipo());
    assertEquals("Servico Teste", itemServico.getDescricao());
    assertEquals(new BigDecimal("100.00"), itemServico.getValorUnitario());

    // 2. From Peca
    Peca peca = mock(Peca.class);
    when(peca.getId()).thenReturn(UUID.randomUUID());
    when(peca.getNome()).thenReturn("Peca Teste");
    when(peca.getPrecoBase()).thenReturn(new BigDecimal("50.00"));

    ItemOrdemServico itemPeca = itemFactory.createFromPeca(peca);
    assertNotNull(itemPeca);
    assertEquals(TipoItem.PECA, itemPeca.getTipo());
    assertEquals("Peca Teste", itemPeca.getDescricao());

    // 3. From Insumo
    Insumo insumo = mock(Insumo.class);
    when(insumo.getId()).thenReturn(UUID.randomUUID());
    when(insumo.getNome()).thenReturn("Insumo Teste");
    when(insumo.getPrecoBase()).thenReturn(new BigDecimal("10.00"));

    ItemOrdemServico itemInsumo = itemFactory.createFromInsumo(insumo);
    assertNotNull(itemInsumo);
    assertEquals(TipoItem.INSUMO, itemInsumo.getTipo());
    assertEquals("Insumo Teste", itemInsumo.getDescricao());
  }
}
