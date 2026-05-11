package com.fiap.mecanica.presentation.dto;

import com.fiap.mecanica.domain.enums.StatusOS;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusOsResponse {

  private UUID id;
  private String codigo;
  private StatusOS status;
  private String statusDescricao;
  private LocalDateTime dataEntrada;
  private LocalDateTime dataAprovacao;
  private LocalDateTime dataFechamento;

  @Builder
  public StatusOsResponse(
      UUID id,
      String codigo,
      StatusOS status,
      String statusDescricao,
      LocalDateTime dataEntrada,
      LocalDateTime dataAprovacao,
      LocalDateTime dataFechamento) {
    this.id = id;
    this.codigo = codigo;
    this.status = status;
    this.statusDescricao = statusDescricao;
    this.dataEntrada = dataEntrada;
    this.dataAprovacao = dataAprovacao;
    this.dataFechamento = dataFechamento;
  }
}
