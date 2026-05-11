package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.exception.DuplicatePlacaException;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceImplTest {

  @Mock private VeiculoRepository repo;

  @InjectMocks private VeiculoServiceImpl service;

  private Veiculo veiculo;
  private UUID clienteId;
  private PlacaVeiculo placa;

  @BeforeEach
  void setUp() {
    clienteId = UUID.randomUUID();
    placa = PlacaVeiculo.of("ABC1234");
    veiculo = new Veiculo(placa, "Corolla", "Toyota", 2020);
    veiculo.setId(UUID.randomUUID());
  }

  @Test
  @DisplayName("Should create Veiculo successfully")
  void shouldCreateVeiculoSuccessfully() {
    when(repo.existsByPlaca(placa)).thenReturn(false);
    when(repo.save(clienteId, veiculo)).thenReturn(veiculo);

    Veiculo result = service.create(clienteId, veiculo);

    assertThat(result).isEqualTo(veiculo);
  }

  @Test
  @DisplayName("Should throw exception when creating Veiculo with duplicate placa")
  void shouldThrowExceptionWhenCreatingVeiculoWithDuplicatePlaca() {
    when(repo.existsByPlaca(placa)).thenReturn(true);

    assertThatThrownBy(() -> service.create(clienteId, veiculo))
        .isInstanceOf(DuplicatePlacaException.class);
  }

  @Test
  @DisplayName("Should get Veiculo by Placa")
  void shouldGetVeiculoByPlaca() {
    String placaStr = placa.value();
    when(repo.findByPlaca(placa)).thenReturn(Optional.of(veiculo));

    Optional<Veiculo> result = service.getByPlaca(placaStr);

    assertThat(result).isPresent().contains(veiculo);
  }

  @Test
  @DisplayName("Should delete Veiculo by Placa")
  void shouldDeleteVeiculoByPlaca() {
    String placaStr = placa.value();
    service.deleteByPlaca(placaStr);
    verify(repo).deleteByPlaca(placa);
  }

  @Test
  @DisplayName("Should list Veiculos by ClienteId")
  void shouldListVeiculosByClienteId() {
    List<Veiculo> veiculos = Collections.singletonList(veiculo);
    when(repo.findAllByClienteId(clienteId)).thenReturn(veiculos);

    List<Veiculo> result = service.listByClienteId(clienteId);

    assertThat(result).isEqualTo(veiculos);
  }

  @Test
  @DisplayName("Should get all Veiculos")
  void shouldGetAllVeiculos() {
    Page<Veiculo> page = Page.empty();
    when(repo.findAll(any(Pageable.class))).thenReturn(page);

    Page<Veiculo> result = service.getAll(Pageable.unpaged());

    assertThat(result).isEqualTo(page);
  }
}
