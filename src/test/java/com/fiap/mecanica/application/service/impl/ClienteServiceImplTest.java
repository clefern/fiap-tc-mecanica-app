package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.exception.DuplicateDocumentoException;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.service.NotificationService;
import com.fiap.mecanica.domain.service.PasswordPolicy;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Documento;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

  @Mock private ClienteRepository repo;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private NotificationService notificationService;
  @Mock private PasswordPolicy passwordPolicy;

  @InjectMocks private ClienteServiceImpl service;

  private Cliente cliente;
  private CPF documento;

  @BeforeEach
  void setUp() {
    documento = CPF.of("65140536374");
    TelefoneBr telefone = TelefoneBr.of("11987654321");
    Endereco endereco = Endereco.of("Rua Teste, 123, Bairro, Cidade - SP, 00000-000");
    cliente =
        new Cliente(
            "Test Client",
            documento,
            TipoPessoa.FISICA,
            Email.of("test@example.com"),
            telefone,
            endereco);
    cliente.setId(UUID.randomUUID());
  }

  @Test
  @DisplayName("Should create Cliente successfully")
  void shouldCreateClienteSuccessfully() {
    String rawPassword = "rawPassword";
    String encodedPassword = "encodedPassword";

    when(repo.existsByDocumento(documento)).thenReturn(false);
    when(passwordPolicy.generateRandomPassword()).thenReturn(rawPassword);
    when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
    when(repo.save(cliente)).thenReturn(cliente);

    Cliente result = service.create(cliente);

    assertThat(result).isEqualTo(cliente);
    assertThat(result.getPassword()).isEqualTo(encodedPassword);
    verify(notificationService).sendWelcomeEmail(cliente, rawPassword);
  }

  @Test
  @DisplayName("Should throw exception when creating Cliente with duplicate document")
  void shouldThrowExceptionWhenCreatingClienteWithDuplicateDocument() {
    when(repo.existsByDocumento(documento)).thenReturn(true);

    assertThatThrownBy(() -> service.create(cliente))
        .isInstanceOf(DuplicateDocumentoException.class);
  }

  @Test
  @DisplayName("Should update Cliente successfully")
  void shouldUpdateClienteSuccessfully() {
    UUID id = cliente.getId();

    when(repo.findByDocumento(documento)).thenReturn(Optional.of(cliente));
    when(repo.save(cliente)).thenReturn(cliente);

    Cliente result = service.update(id, cliente);

    assertThat(result).isEqualTo(cliente);
  }

  @Test
  @DisplayName("Should update Cliente successfully when document is new")
  void shouldUpdateClienteSuccessfullyWhenDocumentIsNew() {
    UUID id = cliente.getId();

    // Simulate that the new document does not exist in DB
    when(repo.findByDocumento(documento)).thenReturn(Optional.empty());
    when(repo.save(cliente)).thenReturn(cliente);

    Cliente result = service.update(id, cliente);

    assertThat(result).isEqualTo(cliente);
  }

  @Test
  @DisplayName("Should throw exception when updating Cliente with duplicate document")
  void shouldThrowExceptionWhenUpdatingClienteWithDuplicateDocument() {
    UUID id = UUID.randomUUID();
    TelefoneBr telefone = TelefoneBr.of("11987654321");
    Endereco endereco = Endereco.of("Rua Existing, 456");
    Cliente existingCliente =
        new Cliente(
            "Existing",
            documento,
            TipoPessoa.FISICA,
            Email.of("existing@example.com"),
            telefone,
            endereco);
    existingCliente.setId(UUID.randomUUID()); // Different ID

    when(repo.findByDocumento(documento)).thenReturn(Optional.of(existingCliente));

    assertThatThrownBy(() -> service.update(id, cliente))
        .isInstanceOf(DuplicateDocumentoException.class);
  }

  @Test
  @DisplayName("Should get Cliente by ID")
  void shouldGetClienteById() {
    UUID id = cliente.getId();
    when(repo.findById(id)).thenReturn(Optional.of(cliente));

    Optional<Cliente> result = service.getById(id);

    assertThat(result).isPresent().contains(cliente);
  }

  @Test
  @DisplayName("Should get Cliente by Documento")
  void shouldGetClienteByDocumento() {
    String docStr = documento.valor();
    when(repo.findByDocumento(any(Documento.class))).thenReturn(Optional.of(cliente));

    Optional<Cliente> result = service.getByDocumento(docStr);

    assertThat(result).isPresent().contains(cliente);
  }

  @Test
  @DisplayName("Should get all Clientes")
  void shouldGetAllClientes() {
    Page<Cliente> page = Page.empty();
    when(repo.findAll(any(Pageable.class))).thenReturn(page);

    Page<Cliente> result = service.getAll(Pageable.unpaged());

    assertThat(result).isEqualTo(page);
  }

  @Test
  @DisplayName("Should delete Cliente")
  void shouldDeleteCliente() {
    UUID id = cliente.getId();
    service.delete(id);
    verify(repo).deleteById(id);
  }
}
