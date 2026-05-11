package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fiap.mecanica.domain.enums.TipoItem;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemOrdemServicoTest {

  @Test
  @DisplayName("Deve calcular subtotal corretamente")
  void deveCalcularSubtotal() {
    ItemOrdemServico item =
        ItemOrdemServico.builder().valorUnitario(new BigDecimal("10.00")).quantidade(5).build();

    assertEquals(new BigDecimal("50.00"), item.getSubtotal());
  }

  @Test
  @DisplayName("Deve retornar zero quando valor unitário for nulo")
  void deveRetornarZeroQuandoValorNulo() {
    ItemOrdemServico item = ItemOrdemServico.builder().valorUnitario(null).quantidade(5).build();

    assertEquals(BigDecimal.ZERO, item.getSubtotal());
  }

  @Test
  @DisplayName("Deve retornar zero quando quantidade for nula")
  void deveRetornarZeroQuandoQuantidadeNula() {
    ItemOrdemServico item =
        ItemOrdemServico.builder().valorUnitario(new BigDecimal("10.00")).quantidade(null).build();

    assertEquals(BigDecimal.ZERO, item.getSubtotal());
  }

  @Test
  @DisplayName("Deve testar builder e getters")
  void deveTestarBuilderEGetters() {
    UUID id = UUID.randomUUID();
    UUID refId = UUID.randomUUID();

    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(id)
            .tipo(TipoItem.PECA)
            .descricao("Peça X")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(2)
            .referenciaId(refId)
            .build();

    assertEquals(id, item.getId());
    assertEquals(TipoItem.PECA, item.getTipo());
    assertEquals("Peça X", item.getDescricao());
    assertEquals(BigDecimal.TEN, item.getValorUnitario());
    assertEquals(2, item.getQuantidade());
    assertEquals(refId, item.getReferenciaId());
  }
}
