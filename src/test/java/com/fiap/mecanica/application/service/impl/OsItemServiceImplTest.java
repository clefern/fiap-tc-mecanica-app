package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.os.OsEstoqueValidator;
import com.fiap.mecanica.application.service.os.OsMecanicoAssigner;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OsItemServiceImplTest {

  @Mock private OrdemServicoRepository repository;
  @Mock private OsEstoqueValidator estoqueValidator;
  @Mock private OsMecanicoAssigner mecanicoAssigner;

  @InjectMocks private OsItemServiceImpl service;

  private UUID osId;
  private UUID mecanicoId;
  private OrdemServico os;

  @BeforeEach
  void setUp() {
    osId = UUID.randomUUID();
    mecanicoId = UUID.randomUUID();
    os = OrdemServico.nova(UUID.randomUUID(), UUID.randomUUID());
    os.setId(osId);
  }

  @Test
  @DisplayName("Deve adicionar item com sucesso")
  void deveAdicionarItemComSucesso() {
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .tipo(TipoItem.PECA)
            .referenciaId(UUID.randomUUID())
            .descricao("Filtro")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(2)
            .build();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.adicionarItem(osId, item, mecanicoId);

    assertThat(result.getItens()).hasSize(1);
    verify(mecanicoAssigner).assign(os, mecanicoId);
    verify(estoqueValidator).validar(item);
  }

  @Test
  @DisplayName("Deve lançar exceção quando OS não encontrada ao adicionar item")
  void deveLancarExcecaoQuandoOsNaoEncontrada() {
    when(repository.findById(osId)).thenReturn(Optional.empty());

    ItemOrdemServico item = ItemOrdemServico.builder().tipo(TipoItem.PECA).quantidade(1).build();

    assertThatThrownBy(() -> service.adicionarItem(osId, item, mecanicoId))
        .isInstanceOf(OrdemServicoNaoEncontradaException.class);
  }

  @Test
  @DisplayName("Deve propagar exceção de estoque insuficiente ao adicionar item")
  void devePropararExcecaoDeEstoqueInsuficiente() {
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .tipo(TipoItem.PECA)
            .referenciaId(UUID.randomUUID())
            .quantidade(10)
            .build();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    doThrow(new EstoqueInsuficienteException("Peca", 10, 5)).when(estoqueValidator).validar(item);

    assertThatThrownBy(() -> service.adicionarItem(osId, item, mecanicoId))
        .isInstanceOf(EstoqueInsuficienteException.class);
  }

  @Test
  @DisplayName("Deve adicionar itens em lote com sucesso")
  void deveAdicionarItensEmLoteComSucesso() {
    ItemOrdemServico item1 =
        ItemOrdemServico.builder()
            .tipo(TipoItem.SERVICO)
            .descricao("Serviço 1")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();
    ItemOrdemServico item2 =
        ItemOrdemServico.builder()
            .tipo(TipoItem.SERVICO)
            .descricao("Serviço 2")
            .valorUnitario(BigDecimal.ONE)
            .quantidade(1)
            .referenciaId(UUID.randomUUID())
            .build();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    OrdemServico result = service.adicionarItensEmLote(osId, List.of(item1, item2), mecanicoId);

    assertThat(result.getItens()).hasSize(2);
    verify(mecanicoAssigner).assign(os, mecanicoId);
    verify(estoqueValidator).validar(item1);
    verify(estoqueValidator).validar(item2);
  }

  @Test
  @DisplayName("Deve atualizar quantidade de item com sucesso")
  void deveAtualizarQuantidadeItemComSucesso() {
    UUID itemId = UUID.randomUUID();
    UUID pecaId = UUID.randomUUID();
    ItemOrdemServico item =
        ItemOrdemServico.builder().tipo(TipoItem.PECA).referenciaId(pecaId).quantidade(1).build();
    item.setId(itemId);
    os.adicionarItem(item);

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    service.atualizarQuantidadeItem(osId, itemId, 5, mecanicoId);

    assertThat(os.getItens().get(0).getQuantidade()).isEqualTo(5);
    verify(estoqueValidator).validar(any(ItemOrdemServico.class));
  }

  @Test
  @DisplayName("Deve lançar exceção quando item não encontrado ao atualizar quantidade")
  void deveLancarExcecaoQuandoItemNaoEncontradoAoAtualizar() {
    when(repository.findById(osId)).thenReturn(Optional.of(os));

    assertThatThrownBy(
            () -> service.atualizarQuantidadeItem(osId, UUID.randomUUID(), 5, mecanicoId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Item não encontrado");
  }

  @Test
  @DisplayName("Deve remover item com sucesso")
  void deveRemoverItemComSucesso() {
    UUID itemId = UUID.randomUUID();
    ItemOrdemServico item = ItemOrdemServico.builder().tipo(TipoItem.PECA).quantidade(1).build();
    item.setId(itemId);
    os.adicionarItem(item);

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    service.removerItem(osId, itemId, mecanicoId);

    assertThat(os.getItens()).isEmpty();
    verify(mecanicoAssigner).assign(os, mecanicoId);
  }

  @Test
  @DisplayName("Deve delegar validação de estoque para OsEstoqueValidator")
  void deveDelegarValidacaoDeEstoqueParaValidator() {
    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .tipo(TipoItem.SERVICO)
            .descricao("Alinhamento")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(1)
            .build();

    when(repository.findById(osId)).thenReturn(Optional.of(os));
    when(repository.save(any(OrdemServico.class))).thenReturn(os);

    service.adicionarItensEmLote(osId, Collections.singletonList(item), mecanicoId);

    verify(estoqueValidator).validar(item);
  }
}
