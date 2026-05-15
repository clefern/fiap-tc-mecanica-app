package com.fiap.mecanica.infra.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.entity.ItemOrdemServicoEntity;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class OrdemServicoEntityMapperTest {

  private OrdemServicoEntityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(OrdemServicoEntityMapper.class);
  }

  @Test
  @DisplayName("Deve mapear lista de itens nula para vazia no toDomain")
  void shouldMapNullItemsToEmptyInDomain() {
    OrdemServicoEntity entity = new OrdemServicoEntity();
    entity.setItens(null);

    OrdemServico domain = mapper.toDomain(entity);
    assertThat(domain).isNotNull();
    assertThat(domain.getItens()).isNull();
  }

  @Test
  @DisplayName("Deve mapear lista de itens nula no toEntity")
  void shouldMapNullItemsInEntity() {
    OrdemServico domain = OrdemServico.builder().itens(null).build();

    OrdemServicoEntity entity = mapper.toEntity(domain);
    assertThat(entity).isNotNull();
    assertThat(entity.getItens()).isEmpty();
  }

  @Test
  @DisplayName("Deve mapear Entity para Domain")
  void shouldMapEntityToDomain() {
    UUID id = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    ItemOrdemServicoEntity itemEntity =
        ItemOrdemServicoEntity.builder()
            .id(itemId)
            .tipo(TipoItem.PECA)
            .descricao("Peca 1")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(2)
            .referenciaId(UUID.randomUUID())
            .build();

    OrdemServicoEntity entity =
        OrdemServicoEntity.builder()
            .id(id)
            .clienteId(clienteId)
            .veiculoId(veiculoId)
            .codigo("OS-123")
            .status(StatusOS.RECEBIDA)
            .valorTotal(BigDecimal.valueOf(20))
            .dataEntrada(now)
            .itens(List.of(itemEntity))
            .build();

    OrdemServico domain = mapper.toDomain(entity);

    assertThat(domain).isNotNull();
    assertThat(domain.getId()).isEqualTo(id);
    assertThat(domain.getClienteId()).isEqualTo(clienteId);
    assertThat(domain.getVeiculoId()).isEqualTo(veiculoId);
    assertThat(domain.getCodigo()).isEqualTo("OS-123");
    assertThat(domain.getStatus()).isEqualTo(StatusOS.RECEBIDA);
    assertThat(domain.getValorTotal()).isEqualTo(BigDecimal.valueOf(20));
    assertThat(domain.getDataEntrada()).isEqualTo(now);
    assertThat(domain.getItens()).hasSize(1);

    ItemOrdemServico itemDomain = domain.getItens().get(0);
    assertThat(itemDomain.getId()).isEqualTo(itemId);
    assertThat(itemDomain.getTipo()).isEqualTo(TipoItem.PECA);
    assertThat(itemDomain.getDescricao()).isEqualTo("Peca 1");
    assertThat(itemDomain.getValorUnitario()).isEqualTo(BigDecimal.TEN);
    assertThat(itemDomain.getQuantidade()).isEqualTo(2);
  }

  @Test
  @DisplayName("Deve mapear Domain para Entity com itens")
  void shouldMapDomainToEntityWithItems() {
    UUID id = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    ItemOrdemServico itemDomain =
        ItemOrdemServico.builder()
            .tipo(TipoItem.PECA)
            .descricao("Peca 1")
            .valorUnitario(BigDecimal.TEN)
            .quantidade(2)
            .referenciaId(UUID.randomUUID())
            .build();
    itemDomain.setId(itemId);

    OrdemServico domain =
        OrdemServico.builder()
            .clienteId(clienteId)
            .veiculoId(veiculoId)
            .observacoes("Observacao")
            .itens(List.of(itemDomain))
            .build();
    domain.setId(id);
    domain.setDataEntrada(now);
    // Setting other fields as needed, though builder handles defaults

    OrdemServicoEntity entity = mapper.toEntity(domain);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getClienteId()).isEqualTo(clienteId);
    assertThat(entity.getVeiculoId()).isEqualTo(veiculoId);
    assertThat(entity.getObservacoes()).isEqualTo("Observacao");
    assertThat(entity.getDataEntrada()).isEqualTo(now);

    assertThat(entity.getItens()).hasSize(1);
    ItemOrdemServicoEntity itemEntity = entity.getItens().get(0);
    assertThat(itemEntity.getId()).isEqualTo(itemId);
    assertThat(itemEntity.getTipo()).isEqualTo(TipoItem.PECA);
    assertThat(itemEntity.getDescricao()).isEqualTo("Peca 1");
    assertThat(itemEntity.getValorUnitario()).isEqualTo(BigDecimal.TEN);
    assertThat(itemEntity.getQuantidade()).isEqualTo(2);

    // Verify bidirectional relationship
    assertThat(itemEntity.getOrdemServico()).isEqualTo(entity);
  }

  @Test
  @DisplayName("updateEntity deve atualizar escalares e preservar id, createdAt e itens")
  void shouldUpdateEntityPreservingIdCreatedAtAndItens() {
    UUID idOriginal = UUID.randomUUID();
    LocalDateTime createdAtOriginal = LocalDateTime.now().minusDays(5);
    ItemOrdemServicoEntity itemOriginal =
        ItemOrdemServicoEntity.builder().id(UUID.randomUUID()).descricao("original").build();

    OrdemServicoEntity managed =
        OrdemServicoEntity.builder()
            .id(idOriginal)
            .codigo("OS-OLD")
            .status(StatusOS.RECEBIDA)
            .valorTotal(BigDecimal.ZERO)
            .observacoes("antiga")
            .createdAt(createdAtOriginal)
            .itens(new java.util.ArrayList<>(List.of(itemOriginal)))
            .build();

    OrdemServico domain =
        OrdemServico.builder()
            .id(UUID.randomUUID())
            .codigo("OS-NEW")
            .status(StatusOS.EM_DIAGNOSTICO)
            .valorTotal(BigDecimal.valueOf(100))
            .observacoes("nova")
            .build();

    mapper.updateEntity(managed, domain);

    assertThat(managed.getId()).isEqualTo(idOriginal);
    assertThat(managed.getCreatedAt()).isEqualTo(createdAtOriginal);
    assertThat(managed.getItens()).containsExactly(itemOriginal);
    assertThat(managed.getCodigo()).isEqualTo("OS-NEW");
    assertThat(managed.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
    assertThat(managed.getValorTotal()).isEqualByComparingTo("100");
    assertThat(managed.getObservacoes()).isEqualTo("nova");
  }

  @Test
  @DisplayName("updateItem deve atualizar campos e preservar id e ordemServico")
  void shouldUpdateItemPreservingIdAndOrdemServico() {
    UUID idOriginal = UUID.randomUUID();
    OrdemServicoEntity osManaged = OrdemServicoEntity.builder().id(UUID.randomUUID()).build();

    ItemOrdemServicoEntity managed =
        ItemOrdemServicoEntity.builder()
            .id(idOriginal)
            .ordemServico(osManaged)
            .descricao("antigo")
            .quantidade(1)
            .valorUnitario(BigDecimal.ONE)
            .tipo(TipoItem.PECA)
            .referenciaId(UUID.randomUUID())
            .build();

    ItemOrdemServico domain =
        ItemOrdemServico.builder()
            .id(UUID.randomUUID()) // tentar sobrescrever id - deve ser ignorado
            .descricao("novo")
            .quantidade(5)
            .valorUnitario(BigDecimal.valueOf(42))
            .tipo(TipoItem.SERVICO)
            .referenciaId(UUID.randomUUID())
            .build();

    mapper.updateItem(managed, domain);

    assertThat(managed.getId()).isEqualTo(idOriginal);
    assertThat(managed.getOrdemServico()).isEqualTo(osManaged);
    assertThat(managed.getDescricao()).isEqualTo("novo");
    assertThat(managed.getQuantidade()).isEqualTo(5);
    assertThat(managed.getValorUnitario()).isEqualByComparingTo("42");
    assertThat(managed.getTipo()).isEqualTo(TipoItem.SERVICO);
    assertThat(managed.getReferenciaId()).isEqualTo(domain.getReferenciaId());
  }
}
