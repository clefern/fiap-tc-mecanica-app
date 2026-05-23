package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.entity.ItemOrdemServicoEntity;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import com.fiap.mecanica.infra.mapper.OrdemServicoEntityMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class JpaOrdemServicoRepositoryAdapterTest {

  @Mock private JpaOrdemServicoRepository jpaRepository;

  @Mock private OrdemServicoEntityMapper mapper;

  private JpaOrdemServicoRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new JpaOrdemServicoRepositoryAdapter(jpaRepository, mapper);
  }

  @Test
  @DisplayName("Deve listar fila de orçamento ordenada")
  void shouldListFilaOrcamento() {
    OrdemServicoEntity entity = new OrdemServicoEntity();
    OrdemServico os = mock(OrdemServico.class);
    Pageable pageable = PageRequest.of(0, 10);

    when(jpaRepository.findByStatusOrderByPrioridadeDescCreatedAtAsc(StatusOS.RECEBIDA, pageable))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDomain(entity)).thenReturn(os);

    Page<OrdemServico> result = adapter.listarFilaOrcamento(pageable);

    assertThat(result.getContent()).hasSize(1).contains(os);
    verify(jpaRepository)
        .findByStatusOrderByPrioridadeDescCreatedAtAsc(StatusOS.RECEBIDA, pageable);
  }

  @Test
  @DisplayName("Deve listar fila de execução ordenada")
  void shouldListFilaExecucao() {
    OrdemServicoEntity entity = new OrdemServicoEntity();
    OrdemServico os = mock(OrdemServico.class);
    Pageable pageable = PageRequest.of(0, 10);

    when(jpaRepository.findByStatusOrderByPrioridadeDescDataAprovacaoAsc(
            StatusOS.APROVADA, pageable))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(mapper.toDomain(entity)).thenReturn(os);

    Page<OrdemServico> result = adapter.listarFilaExecucao(pageable);

    assertThat(result.getContent()).hasSize(1).contains(os);
    verify(jpaRepository)
        .findByStatusOrderByPrioridadeDescDataAprovacaoAsc(StatusOS.APROVADA, pageable);
  }

  @Test
  @DisplayName("Deve inserir nova ordem de serviço quando id é nulo (insert path)")
  void shouldInsertNewOrdemServicoWhenIdIsNull() {
    OrdemServico os = mock(OrdemServico.class);
    OrdemServicoEntity entity = new OrdemServicoEntity();
    OrdemServicoEntity savedEntity = new OrdemServicoEntity();

    when(os.getId()).thenReturn(null);
    when(mapper.toEntity(os)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(os);

    OrdemServico result = adapter.save(os);

    assertThat(result).isNotNull().isEqualTo(os);
    assertThat(entity.getCreatedAt()).isNotNull();
    assertThat(entity.getUpdatedAt()).isNotNull();
    verify(mapper).toEntity(os);
    verify(jpaRepository).save(entity);
    verify(mapper).toDomain(savedEntity);
  }

  @Test
  @DisplayName("Deve buscar ordem de serviço por ID")
  void shouldFindById() {
    UUID id = UUID.randomUUID();
    OrdemServicoEntity entity = new OrdemServicoEntity();
    OrdemServico os = mock(OrdemServico.class);

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(os);

    Optional<OrdemServico> result = adapter.findById(id);

    assertThat(result).isPresent().contains(os);
    verify(jpaRepository).findById(id);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve retornar vazio ao buscar por ID inexistente")
  void shouldReturnEmptyWhenIdNotFound() {
    UUID id = UUID.randomUUID();
    when(jpaRepository.findById(id)).thenReturn(Optional.empty());

    Optional<OrdemServico> result = adapter.findById(id);

    assertThat(result).isEmpty();
    verify(jpaRepository).findById(id);
  }

  @Test
  @DisplayName(
      "Deve atualizar OS existente in-place via reconciliação sem chamar jpaRepository.save")
  void shouldUpdateExistingOrdemServicoUsingManagedReconciliation() {
    UUID osId = UUID.randomUUID();
    UUID itemExistenteId = UUID.randomUUID();

    ItemOrdemServicoEntity itemManaged = new ItemOrdemServicoEntity();
    itemManaged.setId(itemExistenteId);

    OrdemServicoEntity managed = new OrdemServicoEntity();
    managed.setId(osId);
    managed.setCreatedAt(LocalDateTime.now().minusDays(1));
    managed.setItens(new ArrayList<>(List.of(itemManaged)));

    ItemOrdemServico itemExistenteDom =
        ItemOrdemServico.builder()
            .id(itemExistenteId)
            .descricao("existente atualizado")
            .quantidade(3)
            .valorUnitario(BigDecimal.TEN)
            .tipo(TipoItem.PECA)
            .referenciaId(UUID.randomUUID())
            .build();
    ItemOrdemServico itemNovoDom =
        ItemOrdemServico.builder()
            .descricao("novo")
            .quantidade(1)
            .valorUnitario(BigDecimal.ONE)
            .tipo(TipoItem.SERVICO)
            .referenciaId(UUID.randomUUID())
            .build();

    OrdemServico os = mock(OrdemServico.class);
    when(os.getId()).thenReturn(osId);
    when(os.getItens()).thenReturn(List.of(itemExistenteDom, itemNovoDom));

    when(jpaRepository.findWithItensById(osId)).thenReturn(Optional.of(managed));
    ItemOrdemServicoEntity novoMapped = new ItemOrdemServicoEntity();
    when(mapper.toEntityItem(itemNovoDom)).thenReturn(novoMapped);
    OrdemServico domainResult = mock(OrdemServico.class);
    when(mapper.toDomain(managed)).thenReturn(domainResult);

    OrdemServico result = adapter.save(os);

    assertThat(result).isEqualTo(domainResult);
    assertThat(managed.getUpdatedAt()).isNotNull();
    assertThat(managed.getItens()).hasSize(2).contains(itemManaged, novoMapped);
    assertThat(novoMapped.getOrdemServico()).isEqualTo(managed);

    verify(mapper).updateEntity(managed, os);
    verify(mapper).updateItem(itemManaged, itemExistenteDom);
    verify(mapper).toEntityItem(itemNovoDom);
    verify(jpaRepository, never()).save(any(OrdemServicoEntity.class));
    verify(mapper, never()).toEntity(any(OrdemServico.class));
  }

  @Test
  @DisplayName("Deve remover itens órfãos via reconciliação")
  void shouldRemoveOrphanItensOnUpdate() {
    UUID osId = UUID.randomUUID();
    UUID itemMantidoId = UUID.randomUUID();
    UUID itemOrfaoId = UUID.randomUUID();

    ItemOrdemServicoEntity itemMantido = new ItemOrdemServicoEntity();
    itemMantido.setId(itemMantidoId);
    ItemOrdemServicoEntity itemOrfao = new ItemOrdemServicoEntity();
    itemOrfao.setId(itemOrfaoId);

    OrdemServicoEntity managed = new OrdemServicoEntity();
    managed.setId(osId);
    managed.setItens(new ArrayList<>(List.of(itemMantido, itemOrfao)));

    ItemOrdemServico itemMantidoDom = ItemOrdemServico.builder().id(itemMantidoId).build();
    OrdemServico os = mock(OrdemServico.class);
    when(os.getId()).thenReturn(osId);
    when(os.getItens()).thenReturn(List.of(itemMantidoDom));

    when(jpaRepository.findWithItensById(osId)).thenReturn(Optional.of(managed));
    when(mapper.toDomain(managed)).thenReturn(mock(OrdemServico.class));

    adapter.save(os);

    assertThat(managed.getItens()).containsExactly(itemMantido);
    verify(jpaRepository, never()).save(any(OrdemServicoEntity.class));
  }

  @Test
  @DisplayName("Deve cair em insert path quando id existe mas registro não está no BD")
  void shouldFallbackToInsertWhenIdNotFoundInDb() {
    UUID osId = UUID.randomUUID();
    OrdemServico os = mock(OrdemServico.class);
    when(os.getId()).thenReturn(osId);

    when(jpaRepository.findWithItensById(osId)).thenReturn(Optional.empty());

    OrdemServicoEntity entity = new OrdemServicoEntity();
    OrdemServicoEntity savedEntity = new OrdemServicoEntity();
    when(mapper.toEntity(os)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(os);

    OrdemServico result = adapter.save(os);

    assertThat(result).isEqualTo(os);
    verify(jpaRepository).save(entity);
  }

  @Test
  @DisplayName("Deve retornar contagem de registros")
  void shouldCount() {
    long expectedCount = 10L;
    when(jpaRepository.count()).thenReturn(expectedCount);

    long result = adapter.count();

    assertThat(result).isEqualTo(expectedCount);
    verify(jpaRepository).count();
  }

  @Test
  @DisplayName("Deve listar todas as ordens de serviço paginadas")
  void shouldFindAll() {
    Pageable pageable = Pageable.unpaged();
    OrdemServicoEntity entity = new OrdemServicoEntity();
    OrdemServico os = mock(OrdemServico.class);
    Page<OrdemServicoEntity> pageEntity = new PageImpl<>(List.of(entity));

    when(jpaRepository.findAll(pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(entity)).thenReturn(os);

    Page<OrdemServico> result = adapter.findAll(pageable);

    assertThat(result).isNotEmpty().hasSize(1);
    verify(jpaRepository).findAll(pageable);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve buscar ordem de serviço com itens por ID")
  void shouldFindByIdWithItens() {
    UUID id = UUID.randomUUID();
    OrdemServicoEntity entity = new OrdemServicoEntity();
    OrdemServico os = mock(OrdemServico.class);

    when(jpaRepository.findWithItensById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(os);

    Optional<OrdemServico> result = adapter.findByIdWithItens(id);

    assertThat(result).isPresent().contains(os);
    verify(jpaRepository).findWithItensById(id);
    verify(mapper).toDomain(entity);
  }
}
