package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.infra.entity.OrcamentoEntity;
import com.fiap.mecanica.infra.jpa.JpaOrcamentoRepository;
import com.fiap.mecanica.infra.mapper.OrcamentoEntityMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class JpaOrcamentoRepositoryAdapterTest {

  @Mock private JpaOrcamentoRepository jpaRepository;

  @Mock private OrcamentoEntityMapper mapper;

  @InjectMocks private JpaOrcamentoRepositoryAdapter adapter;

  @Test
  @DisplayName("Deve salvar orcamento")
  void deveSalvarOrcamento() {
    Orcamento orcamento = new Orcamento();
    OrcamentoEntity entity = new OrcamentoEntity();

    when(mapper.toEntity(orcamento)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(orcamento);

    Orcamento result = adapter.save(orcamento);

    assertThat(result).isNotNull();
    verify(jpaRepository).save(entity);
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void deveBuscarPorId() {
    UUID id = UUID.randomUUID();
    OrcamentoEntity entity = new OrcamentoEntity();
    Orcamento orcamento = new Orcamento();

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(orcamento);

    Optional<Orcamento> result = adapter.findById(id);

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Deve retornar vazio ao buscar por ID inexistente")
  void deveRetornarVazioQuandoIdInexistente() {
    UUID id = UUID.randomUUID();

    when(jpaRepository.findById(id)).thenReturn(Optional.empty());

    Optional<Orcamento> result = adapter.findById(id);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve buscar por Código")
  void deveBuscarPorCodigo() {
    String codigo = "ORC-123";
    OrcamentoEntity entity = new OrcamentoEntity();
    Orcamento orcamento = new Orcamento();

    when(jpaRepository.findByCodigo(codigo)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(orcamento);

    Optional<Orcamento> result = adapter.findByCodigo(codigo);

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Deve retornar vazio ao buscar por Código inexistente")
  void deveRetornarVazioQuandoCodigoInexistente() {
    String codigo = "ORC-999";

    when(jpaRepository.findByCodigo(codigo)).thenReturn(Optional.empty());

    Optional<Orcamento> result = adapter.findByCodigo(codigo);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve buscar por Ordem de Serviço ID")
  void deveBuscarPorOrdemServicoId() {
    UUID osId = UUID.randomUUID();
    OrcamentoEntity entity = new OrcamentoEntity();
    Orcamento orcamento = new Orcamento();

    when(jpaRepository.findByOrdemServicoId(osId)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(orcamento);

    Optional<Orcamento> result = adapter.findByOrdemServicoId(osId);

    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Deve retornar vazio ao buscar por Ordem de Serviço ID inexistente")
  void deveRetornarVazioQuandoOrdemServicoIdInexistente() {
    UUID osId = UUID.randomUUID();

    when(jpaRepository.findByOrdemServicoId(osId)).thenReturn(Optional.empty());

    Optional<Orcamento> result = adapter.findByOrdemServicoId(osId);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve listar todos paginado")
  void deveListarTodosPaginado() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<OrcamentoEntity> pageEntity = new PageImpl<>(List.of(new OrcamentoEntity()));
    Orcamento orcamento = new Orcamento();

    when(jpaRepository.findAll(pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(any(OrcamentoEntity.class))).thenReturn(orcamento);

    Page<Orcamento> result = adapter.findAll(pageable);

    assertThat(result).isNotEmpty();
    verify(jpaRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Deve deletar por ID")
  void deveDeletarPorId() {
    UUID id = UUID.randomUUID();
    adapter.deleteById(id);
    verify(jpaRepository).deleteById(id);
  }

  @Test
  @DisplayName("Deve listar todos por Ordem de Serviço ID")
  void deveListarTodosPorOrdemServicoId() {
    UUID osId = UUID.randomUUID();
    OrcamentoEntity entity = new OrcamentoEntity();
    Orcamento orcamento = new Orcamento();

    when(jpaRepository.findAllByOrdemServicoId(osId)).thenReturn(List.of(entity));
    when(mapper.toDomain(entity)).thenReturn(orcamento);

    List<Orcamento> result = adapter.findAllByOrdemServicoId(osId);

    assertThat(result).hasSize(1);
    verify(jpaRepository).findAllByOrdemServicoId(osId);
  }

  @Test
  @DisplayName("Deve buscar por Ordem de Serviço ID e Status")
  void deveBuscarPorOrdemServicoIdEStatus() {
    UUID osId = UUID.randomUUID();
    StatusOrcamento status = StatusOrcamento.GERADO;
    OrcamentoEntity entity = new OrcamentoEntity();
    Orcamento orcamento = new Orcamento();

    when(jpaRepository.findByOrdemServicoIdAndStatus(osId, status)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(orcamento);

    Optional<Orcamento> result = adapter.findByOrdemServicoIdAndStatus(osId, status);

    assertThat(result).isPresent();
    verify(jpaRepository).findByOrdemServicoIdAndStatus(osId, status);
  }

  @Test
  @DisplayName("Deve verificar se existe por Ordem de Serviço ID e Status")
  void deveVerificarSeExistePorOrdemServicoIdEStatus() {
    UUID osId = UUID.randomUUID();
    StatusOrcamento status = StatusOrcamento.GERADO;

    when(jpaRepository.existsByOrdemServicoIdAndStatus(osId, status)).thenReturn(true);

    boolean result = adapter.existsByOrdemServicoIdAndStatus(osId, status);

    assertThat(result).isTrue();
    verify(jpaRepository).existsByOrdemServicoIdAndStatus(osId, status);
  }
}
