package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusOS;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrdemServicoResponse {
  private UUID id;
  private UUID clienteId;
  private UUID veiculoId;
  private UUID mecanicoExecucaoId;
  private UUID mecanicoDiagnosticoId;
  private String codigo;
  private StatusOS status;
  private BigDecimal valorTotal;
  private LocalDateTime dataEntrada;
  private LocalDateTime dataPrevisao;
  private LocalDateTime dataFechamento;
  private String observacoes;
  private Prioridade prioridade;
  private LocalDateTime dataAprovacao;
  private List<ItemOrdemServicoResponse> itens;

  @Builder
  public OrdemServicoResponse(
      UUID id,
      UUID clienteId,
      UUID veiculoId,
      UUID mecanicoExecucaoId,
      UUID mecanicoDiagnosticoId,
      String codigo,
      StatusOS status,
      BigDecimal valorTotal,
      LocalDateTime dataEntrada,
      LocalDateTime dataPrevisao,
      LocalDateTime dataFechamento,
      String observacoes,
      Prioridade prioridade,
      LocalDateTime dataAprovacao,
      List<ItemOrdemServicoResponse> itens) {
    this.id = id;
    this.clienteId = clienteId;
    this.veiculoId = veiculoId;
    this.mecanicoExecucaoId = mecanicoExecucaoId;
    this.mecanicoDiagnosticoId = mecanicoDiagnosticoId;
    this.codigo = codigo;
    this.status = status;
    this.valorTotal = valorTotal;
    this.dataEntrada = dataEntrada;
    this.dataPrevisao = dataPrevisao;
    this.dataFechamento = dataFechamento;
    this.observacoes = observacoes;
    this.prioridade = prioridade;
    this.dataAprovacao = dataAprovacao;
    this.itens = itens != null ? new ArrayList<>(itens) : null;
  }

  public void setItens(List<ItemOrdemServicoResponse> itens) {
    this.itens = itens != null ? new ArrayList<>(itens) : null;
  }

  public List<ItemOrdemServicoResponse> getItens() {
    return itens == null ? null : Collections.unmodifiableList(itens);
  }
}
