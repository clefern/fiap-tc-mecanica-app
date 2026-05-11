package com.fiap.mecanica.application.service.estoque.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.EstoqueAlertaEmailService;
import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemEstocavel;
import com.fiap.mecanica.domain.model.Peca;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceImplTest {

  @Mock private PecaService pecaService;
  @Mock private InsumoService insumoService;
  @Mock private EstoqueAlertaEmailService estoqueAlertaEmailService;

  @InjectMocks private EstoqueServiceImpl service;

  private Peca createPeca(UUID id, int qtd, int min, int max) {
    return new Peca(
        id, "Peca Teste", "Desc", BigDecimal.TEN, true, "Fab", "Cod", "Mod", qtd, min, max);
  }

  private Insumo createInsumo(UUID id, int qtd, int min, int max) {
    return new Insumo(id, "Insumo Teste", "Desc", BigDecimal.TEN, true, "L", qtd, min, max);
  }

  @Test
  @DisplayName("Should lower stock for Peca")
  void shouldLowerStockForPeca() {
    UUID id = UUID.randomUUID();
    Peca peca = createPeca(id, 5, 10, 20); // Status CRITICO (5 < 10)

    when(pecaService.registrarBaixaEstoque(id, 5)).thenReturn(peca);

    ItemEstocavel result = service.baixarEstoque(id, TipoItem.PECA, 5);

    assertThat(result).isEqualTo(peca);
    verify(pecaService).registrarBaixaEstoque(id, 5);
    verify(insumoService, never()).registrarBaixaEstoque(any(), anyInt());
    verify(estoqueAlertaEmailService).enviarAlertaEstoqueBaixo(anyList(), anyList());
  }

  @Test
  @DisplayName("Should lower stock for Insumo")
  void shouldLowerStockForInsumo() {
    UUID id = UUID.randomUUID();
    Insumo insumo = createInsumo(id, 10, 20, 50); // Status CRITICO (10 < 20)

    when(insumoService.registrarBaixaEstoque(id, 10)).thenReturn(insumo);

    ItemEstocavel result = service.baixarEstoque(id, TipoItem.INSUMO, 10);

    assertThat(result).isEqualTo(insumo);
    verify(insumoService).registrarBaixaEstoque(id, 10);
    verify(pecaService, never()).registrarBaixaEstoque(any(), anyInt());
    verify(estoqueAlertaEmailService).enviarAlertaEstoqueBaixo(anyList(), anyList());
  }

  @Test
  @DisplayName("Should trigger alert for Ruptura")
  void shouldTriggerAlertForRuptura() {
    UUID id = UUID.randomUUID();
    // Qtd 0 = Ruptura
    Peca peca = createPeca(id, 0, 10, 20);

    when(pecaService.registrarBaixaEstoque(id, 5)).thenReturn(peca);

    ItemEstocavel result = service.baixarEstoque(id, TipoItem.PECA, 5);

    assertThat(result).isEqualTo(peca);
    verify(estoqueAlertaEmailService).enviarAlertaEstoqueBaixo(anyList(), anyList());
  }

  @Test
  @DisplayName("Should trigger alert for Pre-Alerta")
  void shouldTriggerPreAlert() {
    UUID id = UUID.randomUUID();
    // Min 10, Max 110. Range 100. 10% = 10.
    // Pre-Alert limit = 10 + 10 = 20.
    // Qtd 15 is <= 20 and > 10. So Pre-Alert.
    Peca peca = createPeca(id, 15, 10, 110);

    when(pecaService.registrarBaixaEstoque(id, 5)).thenReturn(peca);

    ItemEstocavel result = service.baixarEstoque(id, TipoItem.PECA, 5);

    assertThat(result).isEqualTo(peca);
    verify(estoqueAlertaEmailService).enviarAlertaEstoqueBaixo(anyList(), anyList());
  }

  @Test
  @DisplayName("Should not trigger alert for Normal status")
  void shouldNotTriggerAlertForNormal() {
    UUID id = UUID.randomUUID();
    // Min 10, Max 110. Pre-Alert limit 20.
    // Qtd 50 > 20. Normal.
    Peca peca = createPeca(id, 50, 10, 110);

    when(pecaService.registrarBaixaEstoque(id, 5)).thenReturn(peca);

    ItemEstocavel result = service.baixarEstoque(id, TipoItem.PECA, 5);

    assertThat(result).isEqualTo(peca);
    verify(estoqueAlertaEmailService, never()).enviarAlertaEstoqueBaixo(anyList(), anyList());
  }

  @Test
  @DisplayName("Should do nothing for non-stockable item when lowering stock")
  void shouldDoNothingForNonStockableItemWhenLoweringStock() {
    UUID id = UUID.randomUUID();
    ItemEstocavel result = service.baixarEstoque(id, TipoItem.SERVICO, 5);

    assertThat(result).isNull();
    verify(pecaService, never()).registrarBaixaEstoque(any(), anyInt());
    verify(insumoService, never()).registrarBaixaEstoque(any(), anyInt());
  }

  @Test
  @DisplayName("Should add stock for Peca")
  void shouldAddStockForPeca() {
    UUID id = UUID.randomUUID();
    Peca peca = createPeca(id, 50, 10, 100);

    when(pecaService.registrarEntradaEstoque(id, 5)).thenReturn(peca);

    ItemEstocavel result = service.adicionarEstoque(id, TipoItem.PECA, 5);

    assertThat(result).isEqualTo(peca);
    verify(pecaService).registrarEntradaEstoque(id, 5);
    // Might trigger alert if status is PRE_ALERTA, depending on logic. Assuming normal.
    // Actually verificarAlertasEstoque logs if PRE_ALERTA or CRITICO.
    // 50 is fine (10 min, 100 max). Status OK. No email.
    // verify(estoqueAlertaEmailService, never()).enviarAlertaEstoqueBaixo(anyList(), anyList());
  }

  @Test
  @DisplayName("Should add stock for Insumo")
  void shouldAddStockForInsumo() {
    UUID id = UUID.randomUUID();
    Insumo insumo = createInsumo(id, 100, 20, 200);

    when(insumoService.registrarEntradaEstoque(id, 10)).thenReturn(insumo);

    ItemEstocavel result = service.adicionarEstoque(id, TipoItem.INSUMO, 10);

    assertThat(result).isEqualTo(insumo);
    verify(insumoService).registrarEntradaEstoque(id, 10);
  }

  @Test
  @DisplayName("Should do nothing for non-stockable item when adding stock")
  void shouldDoNothingForNonStockableItemWhenAddingStock() {
    UUID id = UUID.randomUUID();
    ItemEstocavel result = service.adicionarEstoque(id, TipoItem.SERVICO, 5);

    assertThat(result).isNull();
    verify(pecaService, never()).registrarEntradaEstoque(any(), anyInt());
  }

  @Test
  @DisplayName("Should update parameters for Peca")
  void shouldUpdateParametersForPeca() {
    UUID id = UUID.randomUUID();
    Peca peca = createPeca(id, 50, 10, 100);

    when(pecaService.atualizarParametrosEstoque(id, 10, 100)).thenReturn(peca);

    ItemEstocavel result = service.atualizarParametrosEstoque(id, TipoItem.PECA, 10, 100);

    assertThat(result).isEqualTo(peca);
    verify(pecaService).atualizarParametrosEstoque(id, 10, 100);
  }

  @Test
  @DisplayName("Should update parameters for Insumo")
  void shouldUpdateParametersForInsumo() {
    UUID id = UUID.randomUUID();
    Insumo insumo = createInsumo(id, 100, 20, 200);

    when(insumoService.atualizarParametrosEstoque(id, 20, 200)).thenReturn(insumo);

    ItemEstocavel result = service.atualizarParametrosEstoque(id, TipoItem.INSUMO, 20, 200);

    assertThat(result).isEqualTo(insumo);
    verify(insumoService).atualizarParametrosEstoque(id, 20, 200);
  }

  @Test
  @DisplayName("Should do nothing for non-stockable item when updating parameters")
  void shouldDoNothingForNonStockableItemWhenUpdatingParameters() {
    UUID id = UUID.randomUUID();
    ItemEstocavel result = service.atualizarParametrosEstoque(id, TipoItem.SERVICO, 10, 100);

    assertThat(result).isNull();
    verify(pecaService, never()).atualizarParametrosEstoque(any(), anyInt(), anyInt());
  }
}
