package com.fiap.mecanica.application.service.estoque.impl;

import com.fiap.mecanica.application.service.EstoqueAlertaEmailService;
import com.fiap.mecanica.application.service.InsumoService;
import com.fiap.mecanica.application.service.PecaService;
import com.fiap.mecanica.application.service.estoque.EstoqueService;
import com.fiap.mecanica.domain.enums.StatusEstoque;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemEstocavel;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.infra.monitoring.MonitoredOperation;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstoqueServiceImpl implements EstoqueService {

  private final PecaService pecaService;
  private final InsumoService insumoService;
  private final EstoqueAlertaEmailService estoqueAlertaEmailService;

  @Override
  @Transactional
  @MonitoredOperation("estoque.baixar")
  public ItemEstocavel baixarEstoque(UUID referenciaId, TipoItem tipo, int quantidade) {
    log.info(
        "Processando baixa de estoque: tipo={}, referenciaId={}, quantidade={}",
        tipo,
        referenciaId,
        quantidade);

    if (tipo == TipoItem.PECA) {
      return processarBaixaPeca(referenciaId, quantidade);
    } else if (tipo == TipoItem.INSUMO) {
      return processarBaixaInsumo(referenciaId, quantidade);
    } else {
      log.debug("Item não estocável, nenhum ajuste de estoque necessário. Tipo={}", tipo);
      return null;
    }
  }

  @Override
  @Transactional
  @MonitoredOperation("estoque.adicionar")
  public ItemEstocavel adicionarEstoque(UUID referenciaId, TipoItem tipo, int quantidade) {
    log.info(
        "Processando entrada de estoque: tipo={}, referenciaId={}, quantidade={}",
        tipo,
        referenciaId,
        quantidade);

    if (tipo == TipoItem.PECA) {
      return processarEntradaPeca(referenciaId, quantidade);
    } else if (tipo == TipoItem.INSUMO) {
      return processarEntradaInsumo(referenciaId, quantidade);
    } else {
      log.debug("Item não estocável, nenhuma entrada de estoque necessária. Tipo={}", tipo);
      return null;
    }
  }

  @Override
  @Transactional
  @MonitoredOperation("estoque.atualizarParametros")
  public ItemEstocavel atualizarParametrosEstoque(
      UUID referenciaId, TipoItem tipo, Integer estoqueMinimo, Integer estoqueMaximo) {
    log.info(
        "Atualizando parâmetros de estoque: tipo={}, referenciaId={}, minimo={}, maximo={}",
        tipo,
        referenciaId,
        estoqueMinimo,
        estoqueMaximo);

    if (tipo == TipoItem.PECA) {
      return processarAtualizacaoParametrosPeca(referenciaId, estoqueMinimo, estoqueMaximo);
    } else if (tipo == TipoItem.INSUMO) {
      return processarAtualizacaoParametrosInsumo(referenciaId, estoqueMinimo, estoqueMaximo);
    } else {
      log.debug("Item não estocável, nenhum parâmetro de estoque atualizado. Tipo={}", tipo);
      return null;
    }
  }

  private Peca processarEntradaPeca(UUID referenciaId, int quantidade) {
    Peca peca = pecaService.registrarEntradaEstoque(referenciaId, quantidade);
    verificarAlertasEstoque(peca);
    return peca;
  }

  private Insumo processarEntradaInsumo(UUID referenciaId, int quantidade) {
    Insumo insumo = insumoService.registrarEntradaEstoque(referenciaId, quantidade);
    verificarAlertasEstoque(insumo);
    return insumo;
  }

  private Peca processarAtualizacaoParametrosPeca(
      UUID referenciaId, Integer estoqueMinimo, Integer estoqueMaximo) {
    Peca peca = pecaService.atualizarParametrosEstoque(referenciaId, estoqueMinimo, estoqueMaximo);
    verificarAlertasEstoque(peca);
    return peca;
  }

  private Insumo processarAtualizacaoParametrosInsumo(
      UUID referenciaId, Integer estoqueMinimo, Integer estoqueMaximo) {
    Insumo insumo =
        insumoService.atualizarParametrosEstoque(referenciaId, estoqueMinimo, estoqueMaximo);
    verificarAlertasEstoque(insumo);
    return insumo;
  }

  private Peca processarBaixaPeca(UUID referenciaId, int quantidade) {
    Peca peca = pecaService.registrarBaixaEstoque(referenciaId, quantidade);
    verificarAlertasEstoque(peca);
    return peca;
  }

  private Insumo processarBaixaInsumo(UUID referenciaId, int quantidade) {
    Insumo insumo = insumoService.registrarBaixaEstoque(referenciaId, quantidade);
    verificarAlertasEstoque(insumo);
    return insumo;
  }

  private void verificarAlertasEstoque(ItemEstocavel item) {
    StatusEstoque status = item.verificarStatusEstoque();
    if (status == StatusEstoque.CRITICO || status == StatusEstoque.RUPTURA) {
      log.warn(
          "ALERTA DE ESTOQUE BAIXO: Item '{}' (ID: {}) atingiu o nível CRÍTICO ou RUPTURA. Status:"
              + " {}, Atual: {}, Mínimo: {}",
          item.getNome(),
          item.getId(),
          status,
          item.getQuantidadeEstoque(),
          item.getEstoqueMinimo());
      item.aplicarPorTipo(
          peca -> estoqueAlertaEmailService.enviarAlertaEstoqueBaixo(List.of(peca), List.of()),
          insumo -> estoqueAlertaEmailService.enviarAlertaEstoqueBaixo(List.of(), List.of(insumo)));
    } else if (status == StatusEstoque.PRE_ALERTA) {
      log.info(
          "PRÉ-ALERTA DE ESTOQUE: Item '{}' (ID: {}) entrou em zona de alerta. Atual: {}, Mínimo:"
              + " {}, Máximo: {}",
          item.getNome(),
          item.getId(),
          item.getQuantidadeEstoque(),
          item.getEstoqueMinimo(),
          item.getEstoqueMaximo());
      item.aplicarPorTipo(
          peca -> estoqueAlertaEmailService.enviarAlertaEstoqueBaixo(List.of(peca), List.of()),
          insumo -> estoqueAlertaEmailService.enviarAlertaEstoqueBaixo(List.of(), List.of(insumo)));
    }
  }
}
