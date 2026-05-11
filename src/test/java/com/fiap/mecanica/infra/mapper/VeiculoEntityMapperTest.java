package com.fiap.mecanica.infra.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.VeiculoEntity;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

class VeiculoEntityMapperTest {

  private VeiculoEntityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(VeiculoEntityMapper.class);
    ReflectionTestUtils.setField(mapper, "commonMapper", Mappers.getMapper(CommonMapper.class));
  }

  @Test
  @DisplayName("Deve converter VeiculoEntity para Domain corretamente")
  void shouldConvertEntityToDomain() {
    VeiculoEntity entity = new VeiculoEntity();
    entity.setId(UUID.randomUUID());
    entity.setPlaca("ABC1234");
    entity.setMarca("Toyota");
    entity.setModelo("Corolla");
    entity.setAno(2020);

    Veiculo domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(entity.getId(), domain.getId());
    assertEquals(entity.getPlaca(), domain.getPlaca().value());
    assertEquals(entity.getMarca(), domain.getMarca());
    assertEquals(entity.getModelo(), domain.getModelo());
    assertEquals(entity.getAno(), domain.getAno());
  }

  @Test
  @DisplayName("Deve retornar null ao converter VeiculoEntity nulo para Domain")
  void shouldReturnNullWhenConvertingNullEntityToDomain() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  @DisplayName("Deve converter Domain para VeiculoEntity corretamente")
  void shouldConvertDomainToEntity() {
    Veiculo domain = new Veiculo(PlacaVeiculo.of("ABC1234"), "Corolla", "Toyota", 2020);

    VeiculoEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(domain.getPlaca().value(), entity.getPlaca());
    assertEquals(domain.getMarca(), entity.getMarca());
    assertEquals(domain.getModelo(), entity.getModelo());
    assertEquals(domain.getAno(), entity.getAno());
    assertNull(entity.getCliente()); // Cliente não foi passado
  }

  @Test
  @DisplayName("Deve converter Domain para VeiculoEntity com Cliente corretamente")
  void shouldConvertDomainToEntityWithCliente() {
    Veiculo domain = new Veiculo(PlacaVeiculo.of("ABC1234"), "Corolla", "Toyota", 2020);
    ClienteEntity cliente = new ClienteEntity();
    cliente.setId(UUID.randomUUID());

    VeiculoEntity entity = mapper.toEntity(domain, cliente);

    assertNotNull(entity);
    assertEquals(domain.getPlaca().value(), entity.getPlaca());
    assertEquals(domain.getMarca(), entity.getMarca());
    assertEquals(domain.getModelo(), entity.getModelo());
    assertEquals(domain.getAno(), entity.getAno());
    assertEquals(cliente, entity.getCliente());
  }

  @Test
  @DisplayName("Deve retornar null ao converter Domain nulo para Entity")
  void shouldReturnNullWhenConvertingNullDomainToEntity() {
    assertNull(mapper.toEntity(null));
    assertNull(mapper.toEntity(null, new ClienteEntity()));
  }
}
