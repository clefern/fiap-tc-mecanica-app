package com.fiap.mecanica.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.application.service.ClienteService;
import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import com.fiap.mecanica.presentation.dto.ClienteRequest;
import com.fiap.mecanica.presentation.dto.ClienteResponse;
import com.fiap.mecanica.presentation.mapper.ClienteMapper;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ClienteControllerTest {

  @Mock private ClienteService service;

  @Mock private ClienteMapper mapper;

  @InjectMocks private ClienteController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Deve criar cliente com sucesso")
  void shouldCreateCliente() {
    ClienteRequest request = new ClienteRequest();
    request.setNome("João");
    request.setDocumento("39053344705");
    request.setTipoPessoa(TipoPessoa.FISICA);
    request.setEmail("joao@example.com");
    request.setTelefone("11999999999");
    request.setEndereco("Rua A");

    Cliente domainObj =
        new Cliente(
            "João",
            CPF.of("390.533.447-05"),
            TipoPessoa.FISICA,
            Email.of("joao@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));
    Cliente saved =
        new Cliente(
            "João",
            CPF.of("390.533.447-05"),
            TipoPessoa.FISICA,
            Email.of("joao@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));
    saved.setId(UUID.randomUUID());

    ClienteResponse responseDto = new ClienteResponse();
    responseDto.setId(saved.getId());

    when(mapper.toDomain(request)).thenReturn(domainObj);
    when(service.create(any(Cliente.class))).thenReturn(saved);
    when(mapper.toResponse(saved)).thenReturn(responseDto);

    ResponseEntity<ClienteResponse> response = controller.create(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isInstanceOf(ClienteResponse.class);
    verify(service).create(any(Cliente.class));
  }

  @Test
  @DisplayName("Deve criar cliente JURIDICA com sucesso")
  void shouldCreateClienteJuridica() {
    ClienteRequest request = new ClienteRequest();
    request.setNome("Empresa X");
    request.setDocumento("12345678000195");
    request.setTipoPessoa(TipoPessoa.JURIDICA);
    request.setEmail("contato@empresa.com");
    request.setTelefone("11999999999");
    request.setEndereco("Rua B");

    Cliente domainObj =
        new Cliente(
            "Empresa X",
            new CNPJ("12345678000195"),
            TipoPessoa.JURIDICA,
            Email.of("contato@empresa.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua B"));
    Cliente saved =
        new Cliente(
            "Empresa X",
            new CNPJ("12345678000195"),
            TipoPessoa.JURIDICA,
            Email.of("contato@empresa.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua B"));
    saved.setId(UUID.randomUUID());

    ClienteResponse responseDto = new ClienteResponse();
    responseDto.setId(saved.getId());

    when(mapper.toDomain(request)).thenReturn(domainObj);
    when(service.create(any(Cliente.class))).thenReturn(saved);
    when(mapper.toResponse(saved)).thenReturn(responseDto);

    ResponseEntity<ClienteResponse> response = controller.create(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    verify(service).create(any(Cliente.class));
  }

  @Test
  @DisplayName("Deve buscar cliente por ID")
  void shouldGetById() {
    UUID id = UUID.randomUUID();
    Cliente cliente =
        new Cliente(
            "João",
            CPF.of("390.533.447-05"),
            TipoPessoa.FISICA,
            Email.of("joao@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));
    cliente.setId(id);

    ClienteResponse responseDto = new ClienteResponse();
    responseDto.setId(id);

    when(service.getById(id)).thenReturn(Optional.of(cliente));
    when(mapper.toResponse(cliente)).thenReturn(responseDto);

    ResponseEntity<ClienteResponse> response = controller.getById(id);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isInstanceOf(ClienteResponse.class);
  }

  @Test
  @DisplayName("Deve retornar 404 ao buscar ID inexistente")
  void shouldReturn404WhenIdNotFound() {
    UUID id = UUID.randomUUID();
    when(service.getById(id)).thenReturn(Optional.empty());

    ResponseEntity<ClienteResponse> response = controller.getById(id);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Deve buscar cliente por documento")
  void shouldGetByDocumento() {
    String doc = "39053344705";
    Cliente cliente =
        new Cliente(
            "João",
            CPF.of("390.533.447-05"),
            TipoPessoa.FISICA,
            Email.of("joao@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));
    cliente.setId(UUID.randomUUID());

    ClienteResponse responseDto = new ClienteResponse();
    responseDto.setId(cliente.getId());

    when(service.getByDocumento(doc)).thenReturn(Optional.of(cliente));
    when(mapper.toResponse(cliente)).thenReturn(responseDto);

    ResponseEntity<ClienteResponse> response = controller.getByDocumento(doc);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isInstanceOf(ClienteResponse.class);
  }

  @Test
  @DisplayName("Deve retornar 404 ao buscar documento inexistente")
  void shouldReturn404WhenDocumentoNotFound() {
    String doc = "39053344705";
    when(service.getByDocumento(doc)).thenReturn(Optional.empty());

    ResponseEntity<?> response = controller.getByDocumento(doc);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("Deve atualizar cliente")
  void shouldUpdateCliente() {
    UUID id = UUID.randomUUID();
    ClienteRequest request = new ClienteRequest();
    request.setNome("João Silva");
    request.setDocumento("39053344705");
    request.setTipoPessoa(TipoPessoa.FISICA);
    request.setEmail("joao.silva@example.com");
    request.setTelefone("11988888888");
    request.setEndereco("Rua B");

    Cliente domainObj =
        new Cliente(
            "João Silva",
            CPF.of("390.533.447-05"),
            TipoPessoa.FISICA,
            Email.of("joao.silva@example.com"),
            TelefoneBr.of("11988888888"),
            Endereco.of("Rua B"));
    Cliente updated =
        new Cliente(
            "João Silva",
            CPF.of("390.533.447-05"),
            TipoPessoa.FISICA,
            Email.of("joao.silva@example.com"),
            TelefoneBr.of("11988888888"),
            Endereco.of("Rua B"));
    updated.setId(id);

    ClienteResponse responseDto = new ClienteResponse();
    responseDto.setId(id);

    when(mapper.toDomain(request)).thenReturn(domainObj);
    when(service.update(eq(id), any(Cliente.class))).thenReturn(updated);
    when(mapper.toResponse(updated)).thenReturn(responseDto);

    ResponseEntity<?> response = controller.update(id, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isInstanceOf(ClienteResponse.class);
    verify(service).update(eq(id), any(Cliente.class));
  }

  @Test
  @DisplayName("Deve deletar cliente")
  void shouldDeleteCliente() {
    UUID id = UUID.randomUUID();

    ResponseEntity<Void> response = controller.delete(id);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(service).delete(id);
  }

  @Test
  @DisplayName("Deve listar todos")
  void shouldListAll() {
    Cliente domainObj =
        new Cliente(
            "João",
            CPF.of("390.533.447-05"),
            TipoPessoa.FISICA,
            Email.of("joao@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));
    domainObj.setId(UUID.randomUUID());
    ClienteResponse responseDto = new ClienteResponse();
    responseDto.setId(domainObj.getId());

    Page<Cliente> page = new PageImpl<>(List.of(domainObj));
    when(service.getAll(any(Pageable.class))).thenReturn(page);
    when(mapper.toResponse(domainObj)).thenReturn(responseDto);

    ResponseEntity<Page<ClienteResponse>> response = controller.getAll(PageRequest.of(0, 10));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    verify(mapper).toResponse(domainObj);
  }
}
