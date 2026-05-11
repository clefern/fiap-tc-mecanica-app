package com.fiap.mecanica.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.exception.ItemDuplicadoException;
import com.fiap.mecanica.domain.exception.TransicaoStatusInvalidaException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrdemServicoTest {

  @Test
  void emitirOrcamentoDeveLancarQuandoSemItens() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());

    assertThrows(TransicaoStatusInvalidaException.class, os::emitirOrcamento);
  }

  @Test
  void emitirOrcamentoDeveAlterarStatusQuandoTemItens() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);
    os.iniciarDiagnostico();
    os.emitirOrcamento();

    assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());
  }

  @Test
  void iniciarExecucaoDeveLancarQuandoSemMecanicoExecucao() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());

    assertThrows(TransicaoStatusInvalidaException.class, os::iniciarExecucao);
  }

  @Test
  void iniciarExecucaoDeveAlterarStatusQuandoMecanicoAtribuidoEAprovada() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);
    os.iniciarDiagnostico();
    os.emitirOrcamento();
    os.aprovar();
    UUID mecanicoId = UUID.randomUUID();
    os.setMecanicoExecucaoId(mecanicoId);

    os.iniciarExecucao();

    assertEquals(StatusOS.EM_EXECUCAO, os.getStatus());
  }

  @Test
  void cancelarDeveFecharOrdemServicoAPartirDeStatusAtivo() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.iniciarDiagnostico();

    os.cancelar();

    assertEquals(StatusOS.CANCELADA, os.getStatus());
    assertNotNull(os.getDataFechamento());
  }

  @Test
  void atribuirMecanicoDiagnosticoDeveLancarQuandoStatusInvalido() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setStatus(StatusOS.FINALIZADA);

    assertThrows(
        TransicaoStatusInvalidaException.class,
        () -> os.atribuirMecanicoDiagnostico(UUID.randomUUID()));
  }

  @Test
  void atribuirMecanicoExecucaoDeveLancarQuandoStatusFinalizadoEntregueOuCancelado() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setStatus(StatusOS.FINALIZADA);

    assertThrows(
        TransicaoStatusInvalidaException.class,
        () -> os.atribuirMecanicoExecucao(UUID.randomUUID()));
  }

  @Test
  void trocarMecanicoResponsavelDeveUsarMecanicoDiagnosticoEmStatusDeDiagnostico() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.iniciarDiagnostico();
    UUID novoMecanicoId = UUID.randomUUID();

    os.trocarMecanicoResponsavel(novoMecanicoId);

    assertEquals(novoMecanicoId, os.getMecanicoDiagnosticoId());
  }

  @Test
  void trocarMecanicoResponsavelDeveUsarMecanicoExecucaoEmStatusDeExecucao() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);
    os.iniciarDiagnostico();
    os.emitirOrcamento();
    os.aprovar();
    os.setMecanicoExecucaoId(UUID.randomUUID());
    os.iniciarExecucao();
    UUID novoMecanicoId = UUID.randomUUID();

    os.trocarMecanicoResponsavel(novoMecanicoId);

    assertEquals(novoMecanicoId, os.getMecanicoExecucaoId());
  }

  @Test
  void adicionarItemDeveAtualizarValorTotal() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(2)
            .referenciaId(UUID.randomUUID())
            .build();

    os.adicionarItem(item);

    assertEquals(BigDecimal.valueOf(20), os.getValorTotal());
  }

  @Test
  void adicionarItemDeveLancarItemDuplicadoQuandoReferenciaRepetida() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    UUID referenciaId = UUID.randomUUID();
    ItemOrdemServico item1 =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico 1")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(referenciaId)
            .build();
    ItemOrdemServico item2 =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico 2")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(referenciaId)
            .build();
    os.adicionarItem(item1);

    assertThrows(ItemDuplicadoException.class, () -> os.adicionarItem(item2));
  }

  @Test
  void atualizarQuantidadeItemDeveAtualizarValorTotal() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    UUID itemId = UUID.randomUUID();
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(itemId)
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);

    os.atualizarQuantidadeItem(itemId, 3);

    assertEquals(BigDecimal.valueOf(30), os.getValorTotal());
  }

  @Test
  void atualizarQuantidadeItemDeveLancarQuandoItemNaoExiste() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());

    assertThrows(
        IllegalArgumentException.class, () -> os.atualizarQuantidadeItem(UUID.randomUUID(), 2));
  }

  @Test
  void removerItemDeveAtualizarValorTotalParaZero() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    UUID itemId = UUID.randomUUID();
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(itemId)
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);

    os.removerItem(itemId);

    assertTrue(os.getItens().isEmpty());
    assertEquals(BigDecimal.ZERO, os.getValorTotal());
  }

  @Test
  void removerItemDeveLancarQuandoStatusNaoPermiteRemocao() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    UUID itemId = UUID.randomUUID();
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(itemId)
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);
    os.setStatus(StatusOS.EM_EXECUCAO);

    assertThrows(TransicaoStatusInvalidaException.class, () -> os.removerItem(itemId));
  }

  @Test
  void iniciarDiagnosticoDeveLancarQuandoStatusInvalido() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setStatus(StatusOS.FINALIZADA);

    assertThrows(TransicaoStatusInvalidaException.class, os::iniciarDiagnostico);
  }

  @Test
  void emitirOrcamentoDeveLancarQuandoStatusInvalido() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // Status default is RECEBIDA, emitirOrcamento requires EM_DIAGNOSTICO

    assertThrows(TransicaoStatusInvalidaException.class, os::emitirOrcamento);
  }

  @Test
  void aprovarDeveLancarQuandoStatusInvalido() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // Status default is RECEBIDA

    assertThrows(TransicaoStatusInvalidaException.class, os::aprovar);
  }

  @Test
  void finalizarDeveLancarQuandoStatusInvalido() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // Status default is RECEBIDA

    assertThrows(TransicaoStatusInvalidaException.class, os::finalizar);
  }

  @Test
  void entregarDeveLancarQuandoStatusInvalido() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    // Status default is RECEBIDA

    assertThrows(TransicaoStatusInvalidaException.class, os::entregar);
  }

  @Test
  void deveLancarQuandoStatusInicialNaoForRecebida() {
    OrdemServico os = new OrdemServico();
    // status is null
    assertThrows(TransicaoStatusInvalidaException.class, os::iniciarDiagnostico);
  }

  @Test
  void naoDeveLancarQuandoTransicaoParaMesmoStatus() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.iniciarDiagnostico();
    // Already EM_DIAGNOSTICO
    os.iniciarDiagnostico();
    assertEquals(StatusOS.EM_DIAGNOSTICO, os.getStatus());
  }

  @Test
  void naoDeveLancarQuandoCancelarDeQualquerStatusAtivo() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.cancelar();
    assertEquals(StatusOS.CANCELADA, os.getStatus());

    // Cancelar novamente não deve lançar (idempotente ou permitido)
    os.cancelar();
    assertEquals(StatusOS.CANCELADA, os.getStatus());
  }

  @Test
  void deveLancarQuandoTentarMudarStatusDeCancelada() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.cancelar();
    assertThrows(TransicaoStatusInvalidaException.class, os::iniciarDiagnostico);
  }

  @Test
  void deveLancarQuandoTentarMudarStatusDeEntregue() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);

    os.iniciarDiagnostico();
    os.emitirOrcamento();
    os.aprovar();
    os.setMecanicoExecucaoId(UUID.randomUUID());
    os.iniciarExecucao();
    os.finalizar();
    os.entregar();

    assertThrows(TransicaoStatusInvalidaException.class, os::cancelar);
  }

  @Test
  void devePermitirVoltarParaDiagnosticoDeAguardandoAprovacao() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);
    os.iniciarDiagnostico();
    os.emitirOrcamento();
    assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());

    os.iniciarDiagnostico();
    assertEquals(StatusOS.EM_DIAGNOSTICO, os.getStatus());
  }

  @Test
  void devePermitirAdicionarItemQuandoStatusNull() {
    OrdemServico os = new OrdemServico();
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();

    os.adicionarItem(item);

    assertEquals(1, os.getItens().size());
  }

  @Test
  void devePermitirAdicionarItemQuandoEmDiagnostico() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.iniciarDiagnostico();
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();

    os.adicionarItem(item);

    assertEquals(1, os.getItens().size());
  }

  @Test
  void naoDevePermitirAdicionarItemQuandoAguardandoAprovacao() {
    OrdemServico os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    os.adicionarItem(item);
    os.iniciarDiagnostico();
    os.emitirOrcamento();

    ItemOrdemServico item2 =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID())
            .tipo(TipoItem.SERVICO)
            .descricao("Servico 2")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();

    assertThrows(TransicaoStatusInvalidaException.class, () -> os.adicionarItem(item2));
  }
}
