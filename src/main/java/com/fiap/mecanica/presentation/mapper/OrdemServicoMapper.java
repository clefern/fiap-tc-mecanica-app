package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.presentation.dto.AdicionarItemRequest;
import com.fiap.mecanica.presentation.dto.ItemOrdemServicoResponse;
import com.fiap.mecanica.presentation.dto.OrdemServicoResponse;
import com.fiap.mecanica.presentation.dto.StatusOsResponse;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoMapper {

  public OrdemServicoResponse toResponse(OrdemServico domain) {
    if (domain == null) {
      return null;
    }
    return OrdemServicoResponse.builder()
        .id(domain.getId())
        .clienteId(domain.getClienteId())
        .veiculoId(domain.getVeiculoId())
        .mecanicoExecucaoId(domain.getMecanicoExecucaoId())
        .mecanicoDiagnosticoId(domain.getMecanicoDiagnosticoId())
        .codigo(domain.getCodigo())
        .status(domain.getStatus())
        .valorTotal(domain.getValorTotal())
        .dataEntrada(domain.getDataEntrada())
        .dataPrevisao(domain.getDataPrevisao())
        .dataFechamento(domain.getDataFechamento())
        .observacoes(domain.getObservacoes())
        .prioridade(domain.getPrioridade())
        .dataAprovacao(domain.getDataAprovacao())
        .itens(toResponseItems(domain.getItens()))
        .build();
  }

  public StatusOsResponse toStatusResponse(OrdemServico domain) {
    if (domain == null) {
      return null;
    }
    return StatusOsResponse.builder()
        .id(domain.getId())
        .codigo(domain.getCodigo())
        .status(domain.getStatus())
        .statusDescricao(descreverStatus(domain.getStatus()))
        .dataEntrada(domain.getDataEntrada())
        .dataAprovacao(domain.getDataAprovacao())
        .dataFechamento(domain.getDataFechamento())
        .build();
  }

  private String descreverStatus(StatusOS status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case RECEBIDA -> "Recebida";
      case EM_DIAGNOSTICO -> "Em Diagnóstico";
      case AGUARDANDO_APROVACAO -> "Aguardando Aprovação";
      case APROVADA -> "Aprovada";
      case EM_EXECUCAO -> "Em Execução";
      case FINALIZADA -> "Finalizada";
      case ENTREGUE -> "Entregue";
      case CANCELADA -> "Cancelada";
    };
  }

  public ItemOrdemServico toDomain(AdicionarItemRequest request) {
    if (request == null) {
      return null;
    }
    return ItemOrdemServico.builder()
        .id(UUID.randomUUID())
        .tipo(request.getTipo())
        .descricao(request.getDescricao())
        .valorUnitario(request.getValorUnitario())
        .quantidade(request.getQuantidade())
        .referenciaId(request.getReferenciaId())
        .build();
  }

  private List<ItemOrdemServicoResponse> toResponseItems(List<ItemOrdemServico> itens) {
    if (itens == null) {
      return Collections.emptyList();
    }
    return itens.stream().map(this::toResponseItem).toList();
  }

  private ItemOrdemServicoResponse toResponseItem(ItemOrdemServico domain) {
    return ItemOrdemServicoResponse.builder()
        .id(domain.getId())
        .tipo(domain.getTipo())
        .descricao(domain.getDescricao())
        .valorUnitario(domain.getValorUnitario())
        .quantidade(domain.getQuantidade())
        .subtotal(domain.getSubtotal())
        .referenciaId(domain.getReferenciaId())
        .build();
  }
}
