package com.fiap.mecanica.infra.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.TipoItem;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemOrdemServicoEntityTest {

  @Test
  @DisplayName("Deve criar entidade via builder")
  void shouldCreateViaBuilder() {
    UUID id = UUID.randomUUID();
    UUID refId = UUID.randomUUID();

    ItemOrdemServicoEntity entity =
        ItemOrdemServicoEntity.builder()
            .id(id)
            .tipo(TipoItem.PECA)
            .descricao("Peca Teste")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(2)
            .referenciaId(refId)
            .build();

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getTipo()).isEqualTo(TipoItem.PECA);
    assertThat(entity.getDescricao()).isEqualTo("Peca Teste");
    assertThat(entity.getValorUnitario()).isEqualTo(BigDecimal.TEN);
    assertThat(entity.getQuantidade()).isEqualTo(2);
    assertThat(entity.getReferenciaId()).isEqualTo(refId);
  }

  @Test
  @DisplayName("Deve calcular subtotal corretamente")
  void shouldCalculateSubtotal() {
    ItemOrdemServicoEntity entity =
        ItemOrdemServicoEntity.builder()
            .valorUnitario(BigDecimal.valueOf(10.50))
            .quantidade(2)
            .build();

    assertThat(entity.getSubtotal()).isEqualTo(BigDecimal.valueOf(21.00));
  }

  @Test
  @DisplayName("Deve retornar zero no subtotal se valor ou quantidade forem nulos")
  void shouldReturnZeroSubtotalIfNull() {
    ItemOrdemServicoEntity entity = new ItemOrdemServicoEntity();
    assertThat(entity.getSubtotal()).isEqualTo(BigDecimal.ZERO);

    entity.setValorUnitario(BigDecimal.TEN);
    assertThat(entity.getSubtotal()).isEqualTo(BigDecimal.ZERO);

    entity.setValorUnitario(null);
    entity.setQuantidade(1);
    assertThat(entity.getSubtotal()).isEqualTo(BigDecimal.ZERO);
  }
}
