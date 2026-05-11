package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.infra.entity.ServicoEntity;
import com.fiap.mecanica.infra.jpa.JpaServicoRepository;
import com.fiap.mecanica.infra.mapper.ServicoEntityMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
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
class JpaServicoRepositoryAdapterTest {

  @Mock private JpaServicoRepository jpaRepository;

  @Mock private ServicoEntityMapper mapper;

  @InjectMocks private JpaServicoRepositoryAdapter adapter;

  private Servico servico;
  private ServicoEntity entity;

  @BeforeEach
  void setUp() {
    servico =
        new Servico(
            UUID.randomUUID(),
            "Troca de Óleo",
            "Troca de óleo sintético",
            new BigDecimal("150.00"),
            true,
            Duration.ofMinutes(45),
            CategoriaServico.MANUTENCAO_PREVENTIVA);

    entity = new ServicoEntity();
    entity.setId(servico.getId());
    entity.setNome(servico.getNome());
    entity.setDescricao(servico.getDescricao());
    entity.setPrecoBase(servico.getPrecoBase());
    entity.setTempoEstimadoMinutos(servico.getTempoEstimado().toMinutes());
    entity.setCategoria(servico.getCategoria());
    entity.setAtivo(servico.isAtivo());
  }

  @Test
  @DisplayName("Deve salvar serviço")
  void shouldSave() {
    when(mapper.toEntity(servico)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(servico);
    when(jpaRepository.save(any(ServicoEntity.class))).thenReturn(entity);

    Servico result = adapter.save(servico);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(servico.getId());
    assertThat(result.getNome()).isEqualTo(servico.getNome());
    assertThat(result.getTempoEstimado()).isEqualTo(servico.getTempoEstimado());
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldFindById() {
    when(jpaRepository.findById(servico.getId())).thenReturn(Optional.of(entity));
    when(mapper.toDomain(any(ServicoEntity.class))).thenReturn(servico);

    Optional<Servico> result = adapter.findById(servico.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(servico.getId());
  }

  @Test
  @DisplayName("Deve buscar todos")
  void shouldFindAll() {
    Pageable pageable = Pageable.unpaged();
    Page<ServicoEntity> pageEntity = new PageImpl<>(Collections.singletonList(entity));

    when(jpaRepository.findAll(pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(any(ServicoEntity.class))).thenReturn(servico);

    Page<Servico> result = adapter.findAll(pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(servico.getId());
  }

  @Test
  @DisplayName("Deve buscar ativos")
  void shouldFindByAtivoTrue() {
    Pageable pageable = Pageable.unpaged();
    Page<ServicoEntity> pageEntity = new PageImpl<>(Collections.singletonList(entity));

    when(jpaRepository.findByAtivoTrue(pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(any(ServicoEntity.class))).thenReturn(servico);

    Page<Servico> result = adapter.findByAtivoTrue(pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(servico.getId());
  }

  @Test
  @DisplayName("Deve deletar por ID")
  void shouldDelete() {
    doNothing().when(jpaRepository).deleteById(servico.getId());

    adapter.delete(servico.getId());

    verify(jpaRepository).deleteById(servico.getId());
  }
}
