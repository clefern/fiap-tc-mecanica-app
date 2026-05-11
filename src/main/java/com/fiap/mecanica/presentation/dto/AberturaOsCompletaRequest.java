package com.fiap.mecanica.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AberturaOsCompletaRequest {

  @NotNull(message = "O ID do cliente é obrigatório") private UUID clienteId;

  @NotNull(message = "O ID do veículo é obrigatório") private UUID veiculoId;

  @NotBlank(message = "As observações (descrição do problema) são obrigatórias")
  private String observacoes;

  @Valid private List<AdicionarItemRequest> itens;
}
