package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.OsItemService;
import com.fiap.mecanica.application.service.os.OsEstoqueValidator;
import com.fiap.mecanica.application.service.os.OsMecanicoAssigner;
import com.fiap.mecanica.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OsItemServiceImpl implements OsItemService {

  private final OrdemServicoRepository repository;
  private final OsEstoqueValidator estoqueValidator;
  private final OsMecanicoAssigner mecanicoAssigner;

  public OsItemServiceImpl(
      OrdemServicoRepository repository,
      OsEstoqueValidator estoqueValidator,
      OsMecanicoAssigner mecanicoAssigner) {
    this.repository = repository;
    this.estoqueValidator = estoqueValidator;
    this.mecanicoAssigner = mecanicoAssigner;
  }

  @Override
  @Transactional
  public OrdemServico adicionarItem(UUID id, ItemOrdemServico item, UUID mecanicoId) {
    OrdemServico os = findOrThrow(id);
    mecanicoAssigner.assign(os, mecanicoId);
    estoqueValidator.validar(item);
    os.adicionarItem(item);
    return repository.save(os);
  }

  @Override
  @Transactional
  public OrdemServico adicionarItensEmLote(UUID id, List<ItemOrdemServico> itens, UUID mecanicoId) {
    OrdemServico os = findOrThrow(id);
    mecanicoAssigner.assign(os, mecanicoId);
    itens.forEach(
        item -> {
          estoqueValidator.validar(item);
          os.adicionarItem(item);
        });
    return repository.save(os);
  }

  @Override
  @Transactional
  public OrdemServico atualizarQuantidadeItem(
      UUID id, UUID itemId, Integer novaQuantidade, UUID mecanicoId) {
    OrdemServico os = findOrThrow(id);
    mecanicoAssigner.assign(os, mecanicoId);

    ItemOrdemServico itemExistente =
        os.getItens().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + itemId));

    ItemOrdemServico itemParaValidacao =
        ItemOrdemServico.builder()
            .tipo(itemExistente.getTipo())
            .referenciaId(itemExistente.getReferenciaId())
            .quantidade(novaQuantidade)
            .build();

    estoqueValidator.validar(itemParaValidacao);
    os.atualizarQuantidadeItem(itemId, novaQuantidade);
    return repository.save(os);
  }

  @Override
  @Transactional
  public OrdemServico removerItem(UUID id, UUID itemId, UUID mecanicoId) {
    OrdemServico os = findOrThrow(id);
    mecanicoAssigner.assign(os, mecanicoId);
    os.removerItem(itemId);
    return repository.save(os);
  }

  private OrdemServico findOrThrow(UUID id) {
    return repository.findById(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
  }
}
