package com.fiap.mecanica.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.application.service.VeiculoService;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.presentation.dto.VeiculoRequest;
import com.fiap.mecanica.presentation.dto.VeiculoResponse;
import com.fiap.mecanica.presentation.mapper.VeiculoMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class VeiculoControllerTest {

  @Mock private VeiculoService service;

  @Mock private VeiculoMapper mapper;

  @InjectMocks private VeiculoController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Deve criar veículo com sucesso")
  void shouldCreateVeiculo() {
    UUID clienteId = UUID.randomUUID();
    VeiculoRequest request = new VeiculoRequest();
    request.setPlaca("ABC1234");
    request.setMarca("Fiat");
    request.setModelo("Uno");
    request.setAno(2020);

    Veiculo veiculoDomain = new Veiculo(PlacaVeiculo.of("ABC1234"), "Uno", "Fiat", 2020);
    Veiculo saved = new Veiculo(PlacaVeiculo.of("ABC1234"), "Uno", "Fiat", 2020);
    VeiculoResponse responseDto = new VeiculoResponse(null, "ABC1234", "Fiat", "Uno", 2020);

    when(mapper.toDomain(request)).thenReturn(veiculoDomain);
    when(service.create(eq(clienteId), any(Veiculo.class))).thenReturn(saved);
    when(mapper.toResponse(saved)).thenReturn(responseDto);

    ResponseEntity<?> response = controller.create(clienteId, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isInstanceOf(VeiculoResponse.class);
    verify(service).create(eq(clienteId), any(Veiculo.class));
    verify(mapper).toDomain(request);
    verify(mapper).toResponse(saved);
  }

  @Test
  @DisplayName("Deve buscar veículo por placa")
  void shouldGetVeiculoByPlaca() {
    String placa = "ABC1234";
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of(placa), "Uno", "Fiat", 2020);
    VeiculoResponse responseDto = new VeiculoResponse(null, placa, "Fiat", "Uno", 2020);

    when(service.getByPlaca(placa)).thenReturn(Optional.of(veiculo));
    when(mapper.toResponse(veiculo)).thenReturn(responseDto);

    ResponseEntity<VeiculoResponse> response = controller.getByPlaca(placa);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPlaca()).isEqualTo(placa);
  }

  @Test
  @DisplayName("Deve retornar 404 quando veículo não encontrado")
  void shouldReturn404WhenVeiculoNotFound() {
    String placa = "ABC1234";
    when(service.getByPlaca(placa)).thenReturn(Optional.empty());

    ResponseEntity<VeiculoResponse> response = controller.getByPlaca(placa);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Deve deletar veículo")
  void shouldDeleteVeiculo() {
    String placa = "ABC1234";

    ResponseEntity<Void> response = controller.deleteByPlaca(placa);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(service).deleteByPlaca(placa);
  }

  @Test
  @DisplayName("Deve listar veículos de um cliente")
  void shouldListVehicles() {
    UUID clienteId = UUID.randomUUID();
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Uno", "Fiat", 2020);
    VeiculoResponse responseDto = new VeiculoResponse(null, "ABC1234", "Fiat", "Uno", 2020);

    when(service.listByClienteId(clienteId)).thenReturn(List.of(veiculo));
    when(mapper.toResponse(veiculo)).thenReturn(responseDto);

    ResponseEntity<List<VeiculoResponse>> response = controller.listByCliente(clienteId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0).getPlaca()).isEqualTo("ABC1234");
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não houver veículos")
  void shouldReturnEmptyListWhenNoVehiclesFound() {
    UUID clienteId = UUID.randomUUID();

    when(service.listByClienteId(clienteId)).thenReturn(List.of());

    ResponseEntity<List<VeiculoResponse>> response = controller.listByCliente(clienteId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }
}
