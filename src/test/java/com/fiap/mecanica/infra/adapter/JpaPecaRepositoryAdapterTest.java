package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.infra.entity.PecaEntity;
import com.fiap.mecanica.infra.jpa.JpaPecaRepository;
import com.fiap.mecanica.infra.mapper.PecaEntityMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class JpaPecaRepositoryAdapterTest {

  @Mock private JpaPecaRepository jpaRepository;

  @Mock private PecaEntityMapper mapper;

  @InjectMocks private JpaPecaRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Deve salvar peça")
  void shouldSave() {
    Peca peca =
        new Peca(
            null,
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            0,
            0,
            0);
    PecaEntity entity = new PecaEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Peca 1");

    when(mapper.toEntity(peca)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity))
        .thenReturn(
            peca); // Simplified for test, assumes mapper returns same object logic or just checks
    // interaction

    // Wait, mapper.toDomain returns a NEW domain object.
    Peca savedDomain =
        new Peca(
            entity.getId(),
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            0,
            0,
            0);
    when(mapper.toDomain(entity)).thenReturn(savedDomain);

    Peca saved = adapter.save(peca);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo(entity.getId());
    verify(mapper).toEntity(peca);
    verify(jpaRepository).save(entity);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldFindById() {
    UUID id = UUID.randomUUID();
    PecaEntity entity = new PecaEntity();
    entity.setId(id);
    Peca domain =
        new Peca(
            id,
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            0,
            0,
            0);

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Peca> result = adapter.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(id);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve listar todas as peças")
  void shouldFindAll() {
    PecaEntity entity = new PecaEntity();
    Peca domain =
        new Peca(
            UUID.randomUUID(),
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            0,
            0,
            0);

    Pageable pageable = Pageable.unpaged();
    Page<PecaEntity> pageEntity = new PageImpl<>(List.of(entity));

    when(jpaRepository.findAll(pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(entity)).thenReturn(domain);

    Page<Peca> result = adapter.findAll(pageable);

    assertThat(result.getContent()).hasSize(1);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve listar peças ativas")
  void shouldFindByAtivoTrue() {
    PecaEntity entity = new PecaEntity();
    Peca domain =
        new Peca(
            UUID.randomUUID(),
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            0,
            0,
            0);

    Pageable pageable = Pageable.unpaged();
    Page<PecaEntity> pageEntity = new PageImpl<>(List.of(entity));

    when(jpaRepository.findByAtivoTrue(pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(entity)).thenReturn(domain);

    Page<Peca> result = adapter.findByAtivoTrue(pageable);

    assertThat(result.getContent()).hasSize(1);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve pesquisar peças por termo")
  void shouldSearchByTerm() {
    PecaEntity entity = new PecaEntity();
    Peca domain =
        new Peca(
            UUID.randomUUID(),
            "Filtro de Óleo",
            "Desc filtro",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            0,
            0,
            0);

    Pageable pageable = Pageable.unpaged();
    Page<PecaEntity> pageEntity = new PageImpl<>(List.of(entity));

    when(jpaRepository.search("filtro", pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(entity)).thenReturn(domain);

    Page<Peca> result = adapter.search("filtro", pageable);

    assertThat(result.getContent()).hasSize(1);
    verify(jpaRepository).search("filtro", pageable);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve deletar por ID")
  void shouldDelete() {
    UUID id = UUID.randomUUID();
    doNothing().when(jpaRepository).deleteById(id);

    adapter.delete(id);

    verify(jpaRepository).deleteById(id);
  }

  @Test
  @DisplayName("Deve buscar itens com estoque baixo")
  void shouldFindItensComEstoqueBaixo() {
    PecaEntity entity = new PecaEntity();
    Peca domain =
        new Peca(
            UUID.randomUUID(),
            "Peca 1",
            "Desc 1",
            new BigDecimal("100.00"),
            true,
            "Fab 1",
            "Cod 1",
            "Mod 1",
            0,
            0,
            0);

    when(jpaRepository.findEstoqueBaixo()).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    List<Peca> result = adapter.findItensComEstoqueBaixo();

    assertThat(result).hasSize(1);
    verify(jpaRepository).findEstoqueBaixo();
    verify(mapper).toDomain(entity);
  }
}
