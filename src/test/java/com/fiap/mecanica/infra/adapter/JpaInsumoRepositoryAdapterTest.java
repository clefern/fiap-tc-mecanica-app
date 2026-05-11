package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.infra.entity.InsumoEntity;
import com.fiap.mecanica.infra.jpa.JpaInsumoRepository;
import com.fiap.mecanica.infra.mapper.InsumoEntityMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class JpaInsumoRepositoryAdapterTest {

  @Mock private JpaInsumoRepository jpaRepository;
  @Mock private InsumoEntityMapper mapper;

  @InjectMocks private JpaInsumoRepositoryAdapter adapter;

  private Insumo insumo;
  private InsumoEntity entity;
  private UUID id;

  @BeforeEach
  void setUp() {
    id = UUID.randomUUID();
    insumo = mock(Insumo.class);
    entity = mock(InsumoEntity.class);
  }

  @Test
  @DisplayName("Deve salvar insumo")
  void shouldSaveInsumo() {
    when(mapper.toEntity(insumo)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(insumo);

    Insumo result = adapter.save(insumo);

    assertThat(result).isEqualTo(insumo);
    verify(jpaRepository).save(entity);
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldFindById() {
    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(insumo);

    Optional<Insumo> result = adapter.findById(id);

    assertThat(result).isPresent().contains(insumo);
  }

  @Test
  @DisplayName("Deve retornar vazio ao buscar por ID inexistente")
  void shouldReturnEmptyWhenIdNotFound() {
    when(jpaRepository.findById(id)).thenReturn(Optional.empty());

    Optional<Insumo> result = adapter.findById(id);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve listar todos")
  void shouldFindAll() {
    Pageable pageable = Pageable.unpaged();
    Page<InsumoEntity> page = new PageImpl<>(List.of(entity));

    when(jpaRepository.findAll(pageable)).thenReturn(page);
    when(mapper.toDomain(entity)).thenReturn(insumo);

    Page<Insumo> result = adapter.findAll(pageable);

    assertThat(result).isNotEmpty();
    assertThat(result.getContent()).contains(insumo);
  }

  @Test
  @DisplayName("Deve listar ativos")
  void shouldFindActive() {
    Pageable pageable = Pageable.unpaged();
    Page<InsumoEntity> page = new PageImpl<>(List.of(entity));

    when(jpaRepository.findByAtivoTrue(pageable)).thenReturn(page);
    when(mapper.toDomain(entity)).thenReturn(insumo);

    Page<Insumo> result = adapter.findByAtivoTrue(pageable);

    assertThat(result).isNotEmpty();
    assertThat(result.getContent()).contains(insumo);
  }

  @Test
  @DisplayName("Deve pesquisar insumos por termo")
  void shouldSearchByTerm() {
    Pageable pageable = Pageable.unpaged();
    Page<InsumoEntity> page = new PageImpl<>(List.of(entity));

    when(jpaRepository.search("oleo", pageable)).thenReturn(page);
    when(mapper.toDomain(entity)).thenReturn(insumo);

    Page<Insumo> result = adapter.search("oleo", pageable);

    assertThat(result).isNotEmpty();
    assertThat(result.getContent()).contains(insumo);
    verify(jpaRepository).search("oleo", pageable);
  }

  @Test
  @DisplayName("Deve deletar por ID")
  void shouldDeleteById() {
    adapter.delete(id);
    verify(jpaRepository).deleteById(id);
  }

  @Test
  @DisplayName("Deve buscar itens com estoque baixo")
  void shouldFindItensComEstoqueBaixo() {
    when(jpaRepository.findEstoqueBaixo()).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(insumo);

    List<Insumo> result = adapter.findItensComEstoqueBaixo();

    assertThat(result).hasSize(1);
    assertThat(result).contains(insumo);
    verify(jpaRepository).findEstoqueBaixo();
  }
}
