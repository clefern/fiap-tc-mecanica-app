package com.fiap.mecanica.presentation.mapper;

import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.presentation.dto.PecaRequest;
import com.fiap.mecanica.presentation.dto.PecaResponse;
import org.springframework.stereotype.Component;

@Component
public class PecaMapper {

  public Peca toDomain(PecaRequest request) {
    if (request == null) {
      return null;
    }
    return new Peca(
        null, // ID gerado na persistência
        request.getNome(),
        request.getDescricao(),
        request.getPrecoBase(),
        request.isAtivo(),
        request.getFabricante(),
        request.getCodigoFabricante(),
        request.getModelo(),
        request.getQuantidadeEstoque(),
        request.getEstoqueMinimo(),
        request.getEstoqueMaximo());
  }

  public PecaResponse toResponse(Peca peca) {
    if (peca == null) {
      return null;
    }
    return new PecaResponse(
        peca.getId(),
        peca.getNome(),
        peca.getDescricao(),
        peca.getPrecoBase(),
        peca.getFabricante(),
        peca.getCodigoFabricante(),
        peca.getModelo(),
        peca.isAtivo(),
        peca.getQuantidadeEstoque(),
        peca.getEstoqueMinimo(),
        peca.getEstoqueMaximo(),
        peca.verificarStatusEstoque());
  }
}
