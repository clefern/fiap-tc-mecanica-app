package com.fiap.mecanica.infrastructure.integration.estoque.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.fiap.mecanica.application.service.estoque.EstoqueService;
import com.fiap.mecanica.domain.enums.TipoItem;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EstoqueModuleAdapterTest {

  @Mock private EstoqueService estoqueService;
  @InjectMocks private EstoqueModuleAdapter adapter;

  @Test
  @DisplayName("Should delegate stock reduction successfully")
  void shouldDelegateStockReductionSuccessfully() {
    UUID id = UUID.randomUUID();
    int qtd = 10;
    TipoItem tipo = TipoItem.PECA;

    assertThatCode(() -> adapter.baixarEstoque(id, tipo, qtd)).doesNotThrowAnyException();

    verify(estoqueService).baixarEstoque(id, tipo, qtd);
  }

  @Test
  @DisplayName("Should propagate exception when stock reduction fails")
  void shouldPropagateExceptionWhenStockReductionFails() {
    UUID id = UUID.randomUUID();
    int qtd = 10;
    TipoItem tipo = TipoItem.PECA;

    doThrow(new RuntimeException("Stock error")).when(estoqueService).baixarEstoque(id, tipo, qtd);

    assertThatThrownBy(() -> adapter.baixarEstoque(id, tipo, qtd))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Stock error");
  }
}
