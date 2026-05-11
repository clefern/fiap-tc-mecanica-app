package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import com.fiap.mecanica.infra.mapper.OrdemServicoEntityMapper;
import java.time.LocalDateTime;
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
  @DisplayName("Deve salvar ordem de serviço")
  void shouldSaveOrdemServico() {
    OrdemServico os = mock(OrdemServico.class);
    OrdemServicoEntity entity = new OrdemServicoEntity();
    OrdemServicoEntity savedEntity = new OrdemServicoEntity();

    when(mapper.toEntity(os)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(os);

    OrdemServico result = adapter.save(os);

    assertThat(result).isNotNull().isEqualTo(os);
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
  @DisplayName("Deve salvar ordem de serviço existente preservando createdAt")
  void shouldSaveExistingOrdemServico() {
    OrdemServico os = mock(OrdemServico.class);
    OrdemServicoEntity entity = new OrdemServicoEntity();
    entity.setCreatedAt(LocalDateTime.now().minusDays(1));
    OrdemServicoEntity savedEntity = new OrdemServicoEntity();

    when(mapper.toEntity(os)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(os);

    OrdemServico result = adapter.save(os);

    assertThat(result).isNotNull().isEqualTo(os);
    assertThat(entity.getUpdatedAt()).isNotNull();
    verify(mapper).toEntity(os);
    verify(jpaRepository).save(entity);
    verify(mapper).toDomain(savedEntity);
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
