package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.VeiculoEntity;
import com.fiap.mecanica.infra.jpa.JpaClienteRepository;
import com.fiap.mecanica.infra.jpa.JpaVeiculoRepository;
import com.fiap.mecanica.infra.mapper.VeiculoEntityMapper;
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

class JpaVeiculoRepositoryAdapterTest {

  @Mock private JpaVeiculoRepository jpaRepository;

  @Mock private JpaClienteRepository clienteRepository;

  @Mock private VeiculoEntityMapper mapper;

  @InjectMocks private JpaVeiculoRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Deve buscar por ID")
  void shouldFindById() {
    UUID id = UUID.randomUUID();
    VeiculoEntity entity = new VeiculoEntity();
    entity.setId(id);
    entity.setPlaca("ABC1234");
    entity.setModelo("Uno");
    entity.setMarca("Fiat");
    entity.setAno(2020);

    Veiculo domain = new Veiculo(PlacaVeiculo.of("ABC1234"), "Uno", "Fiat", 2020);

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Veiculo> result = adapter.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get().getPlaca().value()).isEqualTo("ABC1234");
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve salvar veículo")
  void shouldSave() {
    UUID clienteId = UUID.randomUUID();
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Uno", "Fiat", 2020);

    ClienteEntity cliente = new ClienteEntity();
    cliente.setId(clienteId);

    VeiculoEntity entity = new VeiculoEntity();
    entity.setId(UUID.randomUUID());
    entity.setPlaca("ABC1234");
    entity.setCliente(cliente);

    when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
    when(mapper.toEntity(veiculo, cliente)).thenReturn(entity);
    when(jpaRepository.save(any(VeiculoEntity.class))).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(veiculo);

    Veiculo saved = adapter.save(clienteId, veiculo);

    assertThat(saved).isNotNull();
    verify(clienteRepository).findById(clienteId);
    verify(mapper).toEntity(veiculo, cliente);
    verify(jpaRepository).save(entity);
  }

  @Test
  @DisplayName("Deve buscar por placa")
  void shouldFindByPlaca() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1234");
    VeiculoEntity entity = new VeiculoEntity();
    entity.setId(UUID.randomUUID());
    entity.setPlaca("ABC1234");
    entity.setModelo("Uno");
    entity.setMarca("Fiat");
    entity.setAno(2020);

    Veiculo domain = new Veiculo(placa, "Uno", "Fiat", 2020);

    when(jpaRepository.findByPlaca(placa.value())).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Veiculo> result = adapter.findByPlaca(placa);

    assertThat(result).isPresent();
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve verificar se existe por placa")
  void shouldExistsByPlaca() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1234");
    when(jpaRepository.existsByPlaca(placa.value())).thenReturn(true);

    boolean exists = adapter.existsByPlaca(placa);
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Deve deletar por ID")
  void shouldDeleteById() {
    UUID id = UUID.randomUUID();
    doNothing().when(jpaRepository).deleteById(id);

    adapter.deleteById(id);

    verify(jpaRepository).deleteById(id);
  }

  @Test
  @DisplayName("Deve deletar por placa")
  void shouldDeleteByPlaca() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1234");
    VeiculoEntity entity = new VeiculoEntity();
    entity.setId(UUID.randomUUID());

    when(jpaRepository.findByPlaca(placa.value())).thenReturn(Optional.of(entity));
    doNothing().when(jpaRepository).deleteById(entity.getId());

    adapter.deleteByPlaca(placa);

    verify(jpaRepository).deleteById(entity.getId());
  }

  @Test
  @DisplayName("Não deve fazer nada ao deletar por placa inexistente")
  void shouldDoNothingWhenDeleteByNonExistentPlaca() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1234");
    when(jpaRepository.findByPlaca(placa.value())).thenReturn(Optional.empty());

    adapter.deleteByPlaca(placa);

    verify(jpaRepository).findByPlaca(placa.value());
  }

  @Test
  @DisplayName("Deve listar todos por cliente")
  void shouldFindAllByClienteId() {
    UUID clienteId = UUID.randomUUID();
    VeiculoEntity entity = new VeiculoEntity();
    entity.setPlaca("ABC1234");
    entity.setModelo("Uno");
    entity.setMarca("Fiat");
    entity.setAno(2020);

    Veiculo domain = new Veiculo(PlacaVeiculo.of("ABC1234"), "Uno", "Fiat", 2020);

    List<VeiculoEntity> listEntity = List.of(entity);

    when(jpaRepository.findAllByClienteId(clienteId)).thenReturn(listEntity);
    when(mapper.toDomain(entity)).thenReturn(domain);

    List<Veiculo> result = adapter.findAllByClienteId(clienteId);

    assertThat(result).hasSize(1);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve verificar se existe por ID e Cliente ID")
  void shouldCheckExistsByIdAndClienteId() {
    UUID id = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    when(jpaRepository.existsByIdAndClienteId(id, clienteId)).thenReturn(true);

    boolean exists = adapter.existsByIdAndClienteId(id, clienteId);

    assertThat(exists).isTrue();
    verify(jpaRepository).existsByIdAndClienteId(id, clienteId);
  }

  @Test
  @DisplayName("Deve listar todos paginado")
  void shouldFindAllPaged() {
    Pageable pageable = Pageable.unpaged();
    VeiculoEntity entity = new VeiculoEntity();
    Veiculo domain = new Veiculo(PlacaVeiculo.of("ABC1234"), "Uno", "Fiat", 2020);
    Page<VeiculoEntity> pageEntity = new PageImpl<>(List.of(entity));

    when(jpaRepository.findAll(pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(entity)).thenReturn(domain);

    Page<Veiculo> result = adapter.findAll(pageable);

    assertThat(result).hasSize(1);
    verify(jpaRepository).findAll(pageable);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve lançar erro ao salvar com cliente inexistente")
  void shouldThrowWhenClienteNotFound() {
    UUID clienteId = UUID.randomUUID();
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Uno", "Fiat", 2020);

    when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adapter.save(clienteId, veiculo))
        .isInstanceOf(ClienteNaoEncontradoException.class);
  }
}
