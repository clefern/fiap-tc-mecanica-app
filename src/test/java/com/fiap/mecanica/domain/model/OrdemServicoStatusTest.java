package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.exception.TransicaoStatusInvalidaException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrdemServicoStatusTest {

  @Test
  @DisplayName("Deve validar fluxo feliz de status")
  void fluxoFeliz() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // Adiciona item para permitir orçamento
    os.adicionarItem(
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Teste")
            .valorUnitario(java.math.BigDecimal.TEN)
            .quantidade(1)
            .build());

    // RECEBIDA -> EM_DIAGNOSTICO
    assertDoesNotThrow(os::iniciarDiagnostico);

    // EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO
    assertDoesNotThrow(os::emitirOrcamento);

    // AGUARDANDO_APROVACAO -> APROVADA
    assertDoesNotThrow(os::aprovar);

    // APROVADA -> EM_EXECUCAO
    os.atribuirMecanicoExecucao(UUID.randomUUID()); // Necessário para execução
    assertDoesNotThrow(os::iniciarExecucao);

    // EM_EXECUCAO -> FINALIZADA
    assertDoesNotThrow(os::finalizar);

    // FINALIZADA -> ENTREGUE
    assertDoesNotThrow(os::entregar);
  }

  @Test
  @DisplayName("Deve impedir transições inválidas (ex: RECEBIDA -> FINALIZADA)")
  void transicoesInvalidas() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());

    // Tentar pular etapas
    assertThrows(
        TransicaoStatusInvalidaException.class,
        os::finalizar,
        "Não deve permitir pular de RECEBIDA para FINALIZADA");

    assertThrows(
        TransicaoStatusInvalidaException.class,
        os::entregar,
        "Não deve permitir pular de RECEBIDA para ENTREGUE");
  }

  @Test
  @DisplayName("Deve impedir retrocesso de status final")
  void retrocessoStatus() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // Adiciona item para permitir orçamento
    os.adicionarItem(
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Teste")
            .valorUnitario(java.math.BigDecimal.TEN)
            .quantidade(1)
            .build());

    // Avança até FINALIZADA legalmente
    os.iniciarDiagnostico();
    os.emitirOrcamento();
    os.aprovar();
    os.atribuirMecanicoExecucao(UUID.randomUUID());
    os.iniciarExecucao();
    os.finalizar();

    assertThrows(
        TransicaoStatusInvalidaException.class,
        os::iniciarExecucao, // Tenta voltar para execução
        "Não deve permitir voltar de FINALIZADA para EM_EXECUCAO");
  }
}
