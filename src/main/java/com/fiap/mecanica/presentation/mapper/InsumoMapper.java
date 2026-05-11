package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.presentation.dto.InsumoRequest;
import com.fiap.mecanica.presentation.dto.InsumoResponse;
import org.springframework.stereotype.Component;

@Component
public class InsumoMapper {

  public Insumo toDomain(InsumoRequest request) {
    if (request == null) {
      return null;
    }
    return new Insumo(
        null,
        request.getNome(),
        request.getDescricao(),
        request.getPrecoBase(),
        request.isAtivo(),
        request.getUnidadeMedida(),
        request.getQuantidadeEstoque(),
        request.getEstoqueMinimo(),
        request.getEstoqueMaximo());
  }

  public InsumoResponse toResponse(Insumo insumo) {
    if (insumo == null) {
      return null;
    }
    return new InsumoResponse(
        insumo.getId(),
        insumo.getNome(),
        insumo.getDescricao(),
        insumo.getPrecoBase(),
        insumo.getUnidadeMedida(),
        insumo.isAtivo(),
        insumo.getQuantidadeEstoque(),
        insumo.getEstoqueMinimo(),
        insumo.getEstoqueMaximo(),
        insumo.verificarStatusEstoque());
  }
}
