package com.fiap.mecanica.domain.model;

import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.exception.ItemDuplicadoException;
import com.fiap.mecanica.domain.exception.TransicaoStatusInvalidaException;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID clienteId;
  private UUID veiculoId;
  private UUID mecanicoExecucaoId;
  private UUID mecanicoDiagnosticoId;
  private String codigo; // Ex: OS-2024-0001
  private StatusOS status;
  private BigDecimal valorTotal;
  private LocalDateTime dataEntrada;
  private LocalDateTime dataPrevisao;
  private LocalDateTime dataFechamento;
  private LocalDateTime dataAprovacao;
  private String observacoes;
  private Prioridade prioridade;

  @Builder.Default private List<ItemOrdemServico> itens = new ArrayList<>();

  // Factory method para nova OS
  public static OrdemServico nova(UUID clienteId, UUID veiculoId) {
    return OrdemServico.builder()
        .id(UUID.randomUUID())
        .clienteId(clienteId)
        .veiculoId(veiculoId)
        .codigo(gerarCodigo())
        .status(StatusOS.RECEBIDA)
        .dataEntrada(LocalDateTime.now())
        .valorTotal(BigDecimal.ZERO)
        .prioridade(Prioridade.NORMAL)
        .itens(new ArrayList<>())
        .build();
  }

  private static String gerarCodigo() {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    int randomSuffix = ThreadLocalRandom.current().nextInt(10000);
    return "OS-%s-%04d".formatted(timestamp, randomSuffix);
  }

  public void iniciarDiagnostico() {
    atualizarStatus(StatusOS.EM_DIAGNOSTICO);
  }

  public void emitirOrcamento() {
    if (this.itens.isEmpty()) {
      throw new TransicaoStatusInvalidaException("Não é possível emitir orçamento sem itens na OS");
    }
    atualizarStatus(StatusOS.AGUARDANDO_APROVACAO);
  }

  public void aprovar() {
    atualizarStatus(StatusOS.APROVADA);
    this.dataAprovacao = LocalDateTime.now();
  }

  public void iniciarExecucao() {
    if (this.mecanicoExecucaoId == null) {
      throw new TransicaoStatusInvalidaException(
          "Não é possível iniciar execução sem mecânico atribuído");
    }
    atualizarStatus(StatusOS.EM_EXECUCAO);
  }

  public void finalizar() {
    atualizarStatus(StatusOS.FINALIZADA);
  }

  public void entregar() {
    atualizarStatus(StatusOS.ENTREGUE);
  }

  public void cancelar() {
    atualizarStatus(StatusOS.CANCELADA);
  }

  private void atualizarStatus(StatusOS novoStatus) {
    validarTransicao(novoStatus);
    this.status = novoStatus;
    if (novoStatus == StatusOS.FINALIZADA
        || novoStatus == StatusOS.ENTREGUE
        || novoStatus == StatusOS.CANCELADA) {
      this.dataFechamento = LocalDateTime.now();
    }
  }

  private void validarTransicao(StatusOS novoStatus) {
    if (this.status == null) {
      // Se status atual é null (não deveria acontecer em OS válida, mas ok), aceita
      // inicial
      if (novoStatus != StatusOS.RECEBIDA) {
        throw new TransicaoStatusInvalidaException("Status inicial deve ser RECEBIDA");
      }
      return;
    }

    if (this.status == novoStatus) {
      return; // Mesma transição ok
    }

    if (this.status == StatusOS.CANCELADA) {
      throw new TransicaoStatusInvalidaException(this.status.name(), novoStatus.name());
    }

    if (this.status == StatusOS.ENTREGUE) {
      throw new TransicaoStatusInvalidaException(this.status.name(), novoStatus.name());
    }

    // Sempre permitir cancelar (exceto se já entregue/finalizada? Requirements diz
    // fluxo normal)
    // Vamos permitir cancelar de qualquer status ativo.
    if (novoStatus == StatusOS.CANCELADA) {
      return;
    }

    boolean transicaoValida = false;

    switch (this.status) {
      case RECEBIDA:
        transicaoValida = novoStatus == StatusOS.EM_DIAGNOSTICO;
        break;
      case EM_DIAGNOSTICO:
        transicaoValida = novoStatus == StatusOS.AGUARDANDO_APROVACAO;
        break;
      case AGUARDANDO_APROVACAO:
        // Pode aprovar ou voltar para diagnóstico (ajuste)
        transicaoValida = novoStatus == StatusOS.APROVADA || novoStatus == StatusOS.EM_DIAGNOSTICO;
        break;
      case APROVADA:
        transicaoValida = novoStatus == StatusOS.EM_EXECUCAO;
        break;
      case EM_EXECUCAO:
        transicaoValida = novoStatus == StatusOS.FINALIZADA;
        break;
      case FINALIZADA:
        transicaoValida = novoStatus == StatusOS.ENTREGUE;
        break;
      default:
        transicaoValida = false;
    }

    if (!transicaoValida) {
      throw new TransicaoStatusInvalidaException(this.status.name(), novoStatus.name());
    }
  }

  public void atribuirMecanicoDiagnostico(UUID mecanicoId) {
    if (this.status != StatusOS.RECEBIDA
        && this.status != StatusOS.EM_DIAGNOSTICO
        && this.status != StatusOS.AGUARDANDO_APROVACAO) {
      throw new TransicaoStatusInvalidaException(
          "Não é possível atribuir mecânico de diagnóstico neste status");
    }
    this.mecanicoDiagnosticoId = mecanicoId;
  }

  public void atribuirMecanicoExecucao(UUID mecanicoId) {
    if (this.status == StatusOS.FINALIZADA
        || this.status == StatusOS.ENTREGUE
        || this.status == StatusOS.CANCELADA) {
      throw new TransicaoStatusInvalidaException(
          "Não é possível atribuir mecânico a OS finalizada, entregue ou cancelada");
    }
    this.mecanicoExecucaoId = mecanicoId;
  }

  public void trocarMecanicoResponsavel(UUID novoMecanicoId) {
    if (this.status == StatusOS.RECEBIDA
        || this.status == StatusOS.EM_DIAGNOSTICO
        || this.status == StatusOS.AGUARDANDO_APROVACAO) {
      atribuirMecanicoDiagnostico(novoMecanicoId);
    } else {
      atribuirMecanicoExecucao(novoMecanicoId);
    }
  }

  public void adicionarItem(ItemOrdemServico item) {
    if (!canAddItem()) {
      throw new TransicaoStatusInvalidaException(
          "Não é possível adicionar itens a uma OS com status: " + this.status);
    }
    if (this.itens == null) {
      this.itens = new ArrayList<>();
    }

    boolean exists =
        this.itens.stream().anyMatch(i -> i.getReferenciaId().equals(item.getReferenciaId()));

    if (exists) {
      throw new ItemDuplicadoException(item.getDescricao(), item.getReferenciaId());
    }

    this.itens.add(item);
    recalcularTotal();
  }

  public void atualizarQuantidadeItem(UUID itemId, Integer novaQuantidade) {
    if (!canAddItem()) { // Reusing logic as modifying items is similar restriction
      throw new TransicaoStatusInvalidaException(
          "Não é possível alterar itens de uma OS com status: " + this.status);
    }

    ItemOrdemServico item =
        this.itens.stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Item não encontrado na OS: " + itemId));

    item.setQuantidade(novaQuantidade);
    recalcularTotal();
  }

  public void removerItem(UUID itemId) {
    if (!canRemoveItem()) {
      throw new TransicaoStatusInvalidaException(
          "Não é possível remover itens de uma OS com status: " + this.status);
    }
    if (this.itens != null) {
      this.itens.removeIf(i -> i.getId().equals(itemId));
      recalcularTotal();
    }
  }

  public boolean canAddItem() {
    return this.status == null
        || this.status == StatusOS.RECEBIDA
        || this.status == StatusOS.EM_DIAGNOSTICO;
  }

  public boolean canRemoveItem() {
    return this.status == null
        || this.status == StatusOS.RECEBIDA
        || this.status == StatusOS.EM_DIAGNOSTICO;
  }

  private void recalcularTotal() {
    if (this.itens == null || this.itens.isEmpty()) {
      this.valorTotal = BigDecimal.ZERO;
      return;
    }
    this.valorTotal =
        this.itens.stream()
            .map(ItemOrdemServico::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
