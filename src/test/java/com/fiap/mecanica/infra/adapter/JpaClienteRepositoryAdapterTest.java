package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.jpa.JpaClienteRepository;
import com.fiap.mecanica.infra.mapper.ClienteEntityMapper;
import java.util.Collections;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class JpaClienteRepositoryAdapterTest {

  @Mock private JpaClienteRepository jpaRepository;

  @Mock private ClienteEntityMapper mapper;

  @InjectMocks private JpaClienteRepositoryAdapter adapter;

  @Test
  @DisplayName("Deve converter entidade FISICA para domínio")
  void shouldConvertFisicaToDomain() {
    UUID id = UUID.randomUUID();
    ClienteEntity entity = new ClienteEntity();
    entity.setId(id);
    entity.setNome("João");
    entity.setDocumento("39053344705");
    entity.setTipoPessoa(TipoPessoa.FISICA.name());
    entity.setEmail("joao@test.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua A");
    entity.setAtivo(true);
    entity.setVeiculos(Collections.emptyList());

    Cliente domain =
        new Cliente(
            "João",
            CPF.of("39053344705"),
            TipoPessoa.FISICA,
            Email.of("joao@test.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));
    domain.setId(id);

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Cliente> result = adapter.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get().getNome()).isEqualTo("João");
    // Verify interaction
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve converter entidade JURIDICA para domínio")
  void shouldConvertJuridicaToDomain() {
    UUID id = UUID.randomUUID();
    ClienteEntity entity = new ClienteEntity();
    entity.setId(id);
    entity.setNome("Empresa");
    entity.setDocumento("12345678000195");
    entity.setTipoPessoa(TipoPessoa.JURIDICA.name());
    entity.setEmail("empresa@test.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua B");
    entity.setAtivo(true);

    Cliente domain =
        new Cliente(
            "Empresa",
            CNPJ.of("12345678000195"),
            TipoPessoa.JURIDICA,
            Email.of("empresa@test.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua B"));
    domain.setId(id);

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Cliente> result = adapter.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get().getNome()).isEqualTo("Empresa");
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve encontrar cliente por documento")
  void shouldFindClienteByDocumento() {
    String doc = "39053344705";
    ClienteEntity entity = new ClienteEntity();
    entity.setDocumento(doc);

    Cliente domain =
        new Cliente(
            "João",
            CPF.of(doc),
            TipoPessoa.FISICA,
            Email.of("test@test.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));

    when(jpaRepository.findByDocumento(doc)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Cliente> result = adapter.findByDocumento(CPF.of(doc));

    assertThat(result).isPresent();
    assertThat(result.get().getDocumento().valor()).isEqualTo(doc);
  }

  @Test
  @DisplayName("Deve retornar true se existe por documento")
  void shouldReturnTrueIfExistsByDocumento() {
    String doc = "39053344705";
    when(jpaRepository.existsByDocumento(doc)).thenReturn(true);

    boolean exists = adapter.existsByDocumento(CPF.of(doc));

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Deve retornar true se existe por ID")
  void shouldReturnTrueIfExistsById() {
    UUID id = UUID.randomUUID();
    when(jpaRepository.existsById(id)).thenReturn(true);

    boolean exists = adapter.existsById(id);

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Deve salvar cliente")
  void shouldSaveCliente() {
    Cliente domain =
        new Cliente(
            "João",
            CPF.of("39053344705"),
            TipoPessoa.FISICA,
            Email.of("test@test.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));

    ClienteEntity entity = new ClienteEntity();
    ClienteEntity savedEntity = new ClienteEntity();
    savedEntity.setId(UUID.randomUUID());

    when(mapper.toEntity(domain)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(domain);

    Cliente result = adapter.save(domain);

    assertThat(result).isNotNull();
    verify(jpaRepository).save(entity);
  }

  @Test
  @DisplayName("Deve deletar cliente por ID")
  void shouldDeleteClienteById() {
    UUID id = UUID.randomUUID();
    adapter.deleteById(id);
    verify(jpaRepository).deleteById(id);
  }

  @Test
  @DisplayName("Deve lidar com cliente inativo")
  void shouldHandleInactiveCliente() {
    UUID id = UUID.randomUUID();
    ClienteEntity entity = new ClienteEntity();
    entity.setId(id);
    entity.setNome("João");
    entity.setDocumento("39053344705");
    entity.setTipoPessoa(TipoPessoa.FISICA.name());
    entity.setAtivo(false);

    Cliente domain =
        new Cliente(
            "João",
            CPF.of("39053344705"),
            TipoPessoa.FISICA,
            Email.of("joao@test.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));
    domain.setId(id);
    domain.desativar();

    when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<Cliente> result = adapter.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get().isAtivo()).isFalse();
  }

  @Test
  @DisplayName("Deve buscar todos os clientes paginados")
  void shouldFindAllClientes() {
    Pageable pageable = Pageable.unpaged();
    ClienteEntity entity = new ClienteEntity();
    Page<ClienteEntity> pageEntity = new PageImpl<>(List.of(entity));
    Cliente domain =
        new Cliente(
            "João",
            CPF.of("39053344705"),
            TipoPessoa.FISICA,
            Email.of("test@test.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua A"));

    when(jpaRepository.findAll(pageable)).thenReturn(pageEntity);
    when(mapper.toDomain(entity)).thenReturn(domain);

    Page<Cliente> result = adapter.findAll(pageable);

    assertThat(result).isNotEmpty();
    assertThat(result.getContent()).hasSize(1);
    verify(jpaRepository).findAll(pageable);
  }
}
