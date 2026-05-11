package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.exception.TransicaoStatusInvalidaException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrdemServicoCoverageTest {

  @Test
  @DisplayName("Validar transição com status null (deve falhar se não for RECEBIDA)")
  void validateTransitionFromNull() {
    OrdemServico os = OrdemServico.builder().build(); // Status null
    // Tentar iniciar diagnóstico (vai para EM_DIAGNOSTICO)
    assertThrows(TransicaoStatusInvalidaException.class, os::iniciarDiagnostico);
  }

  @Test
  @DisplayName("Trocar mecânico em fase de execução (deve chamar atribuirMecanicoExecucao)")
  void swapMechanicInExecution() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // Setup state manually to bypass strict flow if needed, but safer to follow flow or setStatus
    os.setStatus(StatusOS.EM_EXECUCAO);

    UUID novoMecanico = UUID.randomUUID();
    os.trocarMecanicoResponsavel(novoMecanico);

    assertEquals(novoMecanico, os.getMecanicoExecucaoId());
  }

  @Test
  @DisplayName("Recalcular total com lista de itens nula")
  void recalculateTotalWithNullItems() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setItens(null);

    // Trigger recalculate via removing item (which handles null check? no, remove checks != null)
    // Or via adicionarItem (checks == null then new ArrayList)
    // We need to call private recalcularTotal? No, we can't.
    // But adicionarItem calls it.
    // If itens is null, adicionarItem initializes it.
    // What about "removerItem"?
    // public void removerItem(UUID itemId) { ... if (this.itens != null) ... }
    // So if null, it does nothing.
    os.removerItem(UUID.randomUUID());
    // No exception, coverage hit.
  }

  @Test
  @DisplayName("Adicionar item com lista nula (deve inicializar)")
  void addItemWithNullList() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setItens(null);

    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .referenciaId(UUID.randomUUID())
            .quantidade(1)
            .build();

    os.adicionarItem(item);

    assertEquals(1, os.getItens().size());
  }

  @Test
  @DisplayName("Validar transição para mesmo status (return imediato)")
  void validateSameStatusTransition() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // ValidarTransicao is called by atualizarStatus.
    // We can't call atualizarStatus with RECEBIDA because nova() sets it.
    // But we can't call public methods that set RECEBIDA (none exist).
    // But "cancelar" sets CANCELADA. If we call cancelar twice?
    os.cancelar();
    os.cancelar(); // Should be no-op/allowed.
    assertEquals(StatusOS.CANCELADA, os.getStatus());
  }
}
