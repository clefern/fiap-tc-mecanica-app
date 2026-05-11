package com.fiap.mecanica.infra.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.VeiculoEntity;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClienteEntityMapperTest {

  @Mock private VeiculoEntityMapper veiculoMapper;

  private ClienteEntityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(ClienteEntityMapper.class);
    // Inject into manual field
    mapper.veiculoMapper = veiculoMapper;
    // Inject into potential MapStruct generated field (if exists)
    try {
      ReflectionTestUtils.setField(mapper, "veiculoEntityMapper", veiculoMapper);
    } catch (IllegalArgumentException e) {
      // Field might not exist if MapStruct didn't generate it or named it differently
    }

    // Inject CommonMapper
    ReflectionTestUtils.setField(mapper, "commonMapper", Mappers.getMapper(CommonMapper.class));
  }

  @Test
  void toDomain_ShouldMapCorrectly() {
    ClienteEntity entity = new ClienteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("John Doe");
    entity.setDocumento("12345678909");
    entity.setTipoPessoa("FISICA");
    entity.setEmail("john@example.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua Teste");
    entity.setAtivo(true);

    // Mock vehicle mapping
    VeiculoEntity veEntity = new VeiculoEntity();
    List<VeiculoEntity> veiculos = Collections.singletonList(veEntity);
    entity.setVeiculos(veiculos);

    // Mock null return from mapper
    Mockito.when(veiculoMapper.toDomain(veEntity)).thenReturn(null);

    Cliente domain = mapper.toDomain(entity);

    assertThat(domain.getId()).isEqualTo(entity.getId());
    assertThat(domain.getVeiculos()).isEmpty(); // Should be empty because mapper returned null
  }

  @Test
  void toDomain_ShouldMapCorrectly_WithValidVehicle() {
    ClienteEntity entity = new ClienteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("John Doe");
    entity.setDocumento("12345678909");
    entity.setTipoPessoa("FISICA");
    entity.setEmail("john@example.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua Teste");
    entity.setAtivo(true);

    VeiculoEntity veEntity = new VeiculoEntity();
    entity.setVeiculos(Collections.singletonList(veEntity));

    // Use real Veiculo
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Modelo", "Marca", 2020);
    Mockito.when(veiculoMapper.toDomain(veEntity)).thenReturn(veiculo);

    Cliente domain = mapper.toDomain(entity);

    assertThat(domain.getVeiculos()).hasSize(1);
  }

  @Test
  void toDomain_ShouldMapInactiveCorrectly() {
    ClienteEntity entity = new ClienteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("John Doe");
    entity.setDocumento("12345678909");
    entity.setTipoPessoa("FISICA");
    entity.setEmail("john@example.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua Teste");
    entity.setAtivo(false);

    Cliente domain = mapper.toDomain(entity);

    assertThat(domain.isAtivo()).isFalse();
  }

  @Test
  void toDomain_ShouldHandleLazyInitializationException() {
    ClienteEntity entity = Mockito.spy(new ClienteEntity());
    entity.setId(UUID.randomUUID());
    entity.setNome("John Doe");
    entity.setDocumento("12345678909");
    entity.setTipoPessoa("FISICA");
    entity.setEmail("john@example.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua Teste");
    entity.setAtivo(true);

    List<VeiculoEntity> mockList = Mockito.mock(List.class);
    Mockito.doThrow(new LazyInitializationException("Lazy loaded"))
        .when(mockList)
        .forEach(ArgumentMatchers.any());

    Mockito.doReturn(mockList).when(entity).getVeiculos();

    Assertions.assertThatThrownBy(() -> mapper.toDomain(entity))
        .isInstanceOf(LazyInitializationException.class);
  }

  @Test
  void toDomain_ShouldHandleGenericExceptionInMapping() {
    ClienteEntity entity = Mockito.spy(new ClienteEntity());
    entity.setId(UUID.randomUUID());
    entity.setNome("John Doe");
    entity.setDocumento("12345678909");
    entity.setTipoPessoa("FISICA");
    entity.setEmail("john@example.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua Teste");
    entity.setAtivo(true);

    List<VeiculoEntity> mockList = Mockito.mock(List.class);
    Mockito.doThrow(new RuntimeException("Generic error"))
        .when(mockList)
        .forEach(ArgumentMatchers.any());

    Mockito.doReturn(mockList).when(entity).getVeiculos();

    Assertions.assertThatThrownBy(() -> mapper.toDomain(entity))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Generic error");
  }

  @Test
  void toEntity_ShouldMapCorrectly() {
    Cliente domain =
        new Cliente(
            "John Doe",
            CPF.of("12345678909"),
            TipoPessoa.FISICA,
            Email.of("john@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua Teste"));
    domain.setId(UUID.randomUUID());

    ClienteEntity entity = mapper.toEntity(domain);

    assertThat(entity.getId()).isEqualTo(domain.getId());
    assertThat(entity.getNome()).isEqualTo(domain.getNome());
    assertThat(entity.getDocumento()).isEqualTo(domain.getDocumento().valor());
  }

  @Test
  void toEntity_ShouldHandleNullFromVeiculoMapper() {
    Cliente domain =
        new Cliente(
            "John Doe",
            CPF.of("12345678909"),
            TipoPessoa.FISICA,
            Email.of("john@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua Teste"));
    domain.setId(UUID.randomUUID());

    // Use real Veiculo to avoid NPE in adicionarVeiculo
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Modelo", "Marca", 2020);
    domain.adicionarVeiculo(veiculo);

    // Mock null return from mapper
    Mockito.when(veiculoMapper.toEntity(veiculo)).thenReturn(null);

    ClienteEntity entity = mapper.toEntity(domain);

    assertThat(entity).isNotNull();
    assertThat(entity.getVeiculos()).isEmpty();
  }

  @Test
  void toEntity_ShouldMapWithVehicles() {
    Cliente domain =
        new Cliente(
            "John Doe",
            CPF.of("12345678909"),
            TipoPessoa.FISICA,
            Email.of("john@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua Teste"));
    domain.setId(UUID.randomUUID());

    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Modelo", "Marca", 2020);
    domain.adicionarVeiculo(veiculo);

    VeiculoEntity veEntity = new VeiculoEntity();
    Mockito.when(veiculoMapper.toEntity(veiculo)).thenReturn(veEntity);

    ClienteEntity entity = mapper.toEntity(domain);

    assertThat(entity).isNotNull();
    assertThat(entity.getVeiculos()).hasSize(1);
    assertThat(entity.getVeiculos().get(0)).isEqualTo(veEntity);
    assertThat(veEntity.getCliente())
        .isEqualTo(entity); // Verify bidirectional relationship setting
  }

  @Test
  void toDomain_ShouldReturnNullWhenEntityIsNull() {
    assertThat(mapper.toDomain(null)).isNull();
  }

  @Test
  void toDomain_ShouldHandleNullVehiclesList() {
    ClienteEntity entity = new ClienteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("John Doe");
    entity.setDocumento("12345678909");
    entity.setTipoPessoa("FISICA");
    entity.setEmail("john@example.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua Teste");
    entity.setAtivo(true);
    entity.setVeiculos(null); // Explicitly null

    Cliente domain = mapper.toDomain(entity);

    assertThat(domain).isNotNull();
    assertThat(domain.getVeiculos()).isEmpty();
  }

  @Test
  void toEntity_ShouldReturnNullWhenDomainIsNull() {
    assertThat(mapper.toEntity(null)).isNull();
  }

  @Test
  void toDomain_ShouldMapJuridicaCorrectly() {
    ClienteEntity entity = new ClienteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Empresa Ltda");
    entity.setDocumento("00.000.000/0001-91"); // Valid CNPJ (Banco do Brasil)
    entity.setTipoPessoa("JURIDICA");
    entity.setEmail("empresa@example.com");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua Teste");
    entity.setAtivo(true);

    Cliente domain = mapper.toDomain(entity);

    assertThat(domain.getTipo()).isEqualTo(TipoPessoa.JURIDICA);
    assertThat(domain.getDocumento()).isInstanceOf(CNPJ.class);
    assertThat(domain.getDocumento().valor())
        .isEqualTo("00000000000191"); // CNPJ stores clean value
  }
}
