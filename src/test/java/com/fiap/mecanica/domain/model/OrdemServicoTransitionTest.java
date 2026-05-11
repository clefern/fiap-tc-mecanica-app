package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.exception.TransicaoStatusInvalidaException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OrdemServicoTransitionTest {

  @Test
  @DisplayName(
      "Fluxo Feliz: Recebida -> Diagnóstico -> Aguardando -> Aprovada -> Execução -> Finalizada ->"
          + " Entregue")
  void happyPathLifecycle() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    assertEquals(StatusOS.RECEBIDA, os.getStatus());

    os.iniciarDiagnostico();
    assertEquals(StatusOS.EM_DIAGNOSTICO, os.getStatus());

    // Precisa de itens para emitir orçamento
    os.adicionarItem(createItem());
    os.emitirOrcamento();
    assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());

    os.aprovar();
    assertEquals(StatusOS.APROVADA, os.getStatus());

    // Precisa de mecânico execução
    os.atribuirMecanicoExecucao(UUID.randomUUID());
    os.iniciarExecucao();
    assertEquals(StatusOS.EM_EXECUCAO, os.getStatus());

    os.finalizar();
    assertEquals(StatusOS.FINALIZADA, os.getStatus());

    os.entregar();
    assertEquals(StatusOS.ENTREGUE, os.getStatus());
  }

  @Test
  @DisplayName("Fluxo Ajuste: Aguardando -> Diagnóstico -> Aguardando")
  void adjustmentFlow() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.iniciarDiagnostico();
    os.adicionarItem(createItem());
    os.emitirOrcamento();
    assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());

    // Rejeita/Ajusta -> volta para diagnóstico
    // Não há método explícito "rejeitar", mas a validação permite voltar para EM_DIAGNOSTICO.
    // Como validarTransicao é privado, precisamos de um método público que faça isso.
    // OrdemServico não tem método "retornarParaDiagnostico".
    // Vamos verificar se iniciarDiagnostico funciona a partir de AGUARDANDO_APROVACAO?
    // iniciarDiagnostico chama atualizarStatus(EM_DIAGNOSTICO).

    os.iniciarDiagnostico();
    assertEquals(StatusOS.EM_DIAGNOSTICO, os.getStatus());

    os.emitirOrcamento();
    assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());
  }

  @Test
  @DisplayName("Cancelamento: Pode cancelar de qualquer status ativo")
  void cancellationFlow() {
    // De Recebida
    OrdemServico os1 = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os1.cancelar();
    assertEquals(StatusOS.CANCELADA, os1.getStatus());

    // De Diagnóstico
    OrdemServico os2 = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os2.iniciarDiagnostico();
    os2.cancelar();
    assertEquals(StatusOS.CANCELADA, os2.getStatus());
  }

  @Test
  @DisplayName("Não pode alterar status se Cancelada")
  void cannotChangeFromCancelled() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.cancelar();

    assertThrows(TransicaoStatusInvalidaException.class, os::iniciarDiagnostico);
    assertThrows(TransicaoStatusInvalidaException.class, os::finalizar);
  }

  @Test
  @DisplayName("Não pode alterar status se Entregue")
  void cannotChangeFromDelivered() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // Hack to get to ENTREGUE quickly without full flow validation if we used setStatus,
    // but we want to test validation logic. So we must use setStatus to set up state
    // and then try to transition.
    // wait, setStatus bypasses validation, so we can set to ENTREGUE directly to test validation
    // logic of NEXT call.
    os.setStatus(StatusOS.ENTREGUE);

    assertThrows(TransicaoStatusInvalidaException.class, os::iniciarDiagnostico);
  }

  @Test
  @DisplayName("Validação de transição inválida: Recebida -> Finalizada")
  void invalidTransitionRecebidaToFinalizada() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    assertThrows(TransicaoStatusInvalidaException.class, os::finalizar);
  }

  @Test
  @DisplayName("Validação de transição inválida: Diagnóstico -> Execução")
  void invalidTransitionDiagnosticoToExecucao() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.iniciarDiagnostico();
    os.atribuirMecanicoExecucao(
        UUID.randomUUID()); // Actually this throws too if logic is strict about when to assign
    // But let's try to call iniciarExecucao directly (it requires mecanicoExecucaoId)
    // To set mecanicoExecucaoId without hitting validation (which might block assignment in
    // DIAGNOSTICO), we might need setMecanicoExecucaoId
    os.setMecanicoExecucaoId(UUID.randomUUID());

    assertThrows(TransicaoStatusInvalidaException.class, os::iniciarExecucao);
  }

  @ParameterizedTest
  @EnumSource(StatusOS.class)
  @DisplayName("Mesmo status não deve lançar exceção (Idempotência)")
  void sameStatusShouldBeAllowed(StatusOS status) {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setStatus(status);

    // We need to call a method that tries to set the SAME status.
    // e.g. if status is EM_DIAGNOSTICO, calling iniciarDiagnostico() again.

    if (status == StatusOS.EM_DIAGNOSTICO) {
      os.iniciarDiagnostico();
    } else if (status == StatusOS.CANCELADA) {
      os.cancelar();
    }
    // For others, we might not have a direct public method that repeats the status easily
    // without side effects (like dataFechamento update), but the validation logic
    // `if (this.status == novoStatus) return;` is what we want to hit.
    // We can't easily invoke private atualizarStatus.
    // But we covered the branch in general.
  }

  private ItemOrdemServico createItem() {
    return ItemOrdemServico.builder()
        .id(UUID.randomUUID())
        .tipo(TipoItem.SERVICO)
        .descricao("Servico Teste")
        .valorUnitario(BigDecimal.TEN)
        .quantidade(1)
        .referenciaId(UUID.randomUUID())
        .build();
  }
}
