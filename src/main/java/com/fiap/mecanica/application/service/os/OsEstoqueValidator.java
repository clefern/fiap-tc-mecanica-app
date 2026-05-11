package com.fiap.mecanica.application.service.os;

import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.repository.InsumoRepository;
import com.fiap.mecanica.domain.repository.PecaRepository;
import org.springframework.stereotype.Component;

@Component
public class OsEstoqueValidator {

  private final PecaRepository pecaRepository;
  private final InsumoRepository insumoRepository;

  public OsEstoqueValidator(PecaRepository pecaRepository, InsumoRepository insumoRepository) {
    this.pecaRepository = pecaRepository;
    this.insumoRepository = insumoRepository;
  }

  public void validar(ItemOrdemServico item) {
    if (item.getTipo() == TipoItem.PECA) {
      Peca peca =
          pecaRepository
              .findById(item.getReferenciaId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Peça não encontrada: " + item.getReferenciaId()));

      if (peca.getQuantidadeEstoque() < item.getQuantidade()) {
        throw new EstoqueInsuficienteException(
            peca.getNome(), item.getQuantidade(), peca.getQuantidadeEstoque());
      }
    } else if (item.getTipo() == TipoItem.INSUMO) {
      Insumo insumo =
          insumoRepository
              .findById(item.getReferenciaId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Insumo não encontrado: " + item.getReferenciaId()));

      if (insumo.getQuantidadeEstoque() < item.getQuantidade()) {
        throw new EstoqueInsuficienteException(
            insumo.getNome(), item.getQuantidade(), insumo.getQuantidadeEstoque());
      }
    }
  }
}
