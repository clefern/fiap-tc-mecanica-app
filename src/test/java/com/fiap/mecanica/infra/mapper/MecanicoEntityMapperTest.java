package com.fiap.mecanica.infra.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.entity.MecanicoEntity;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

class MecanicoEntityMapperTest {

  private MecanicoEntityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(MecanicoEntityMapper.class);
    ReflectionTestUtils.setField(mapper, "commonMapper", Mappers.getMapper(CommonMapper.class));
  }

  @Test
  void toDomain() {
    MecanicoEntity entity = new MecanicoEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("João Silva");
    entity.setCpf("39053344705");
    entity.setEmail("joao@test.com");
    entity.setEspecialidade("Motor");
    entity.setPassword("secret");
    entity.setAtivo(true);

    Mecanico domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(entity.getId(), domain.getId());
    assertEquals(entity.getNome(), domain.getNome());
    assertEquals(entity.getCpf(), domain.getCpf().valor());
    assertEquals(entity.getEmail(), domain.getEmail().value());
    assertEquals(entity.getEspecialidade(), domain.getEspecialidade());
    assertEquals(entity.getPassword(), domain.getPassword());
    assertTrue(domain.isAtivo());
  }

  @Test
  void toDomain_ShouldMapInactiveCorrectly() {
    MecanicoEntity entity = new MecanicoEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("João Silva");
    entity.setCpf("39053344705");
    entity.setEmail("joao@test.com");
    entity.setEspecialidade("Motor");
    entity.setPassword("secret");
    entity.setAtivo(false);

    Mecanico domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertFalse(domain.isAtivo());
  }

  @Test
  void toEntity() {
    Mecanico domain =
        new Mecanico("João Silva", CPF.of("39053344705"), Email.of("joao@test.com"), "Motor");
    domain.setId(UUID.randomUUID());
    domain.setPassword("secret");
    domain.ativar();

    MecanicoEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(domain.getId(), entity.getId());
    assertEquals(domain.getNome(), entity.getNome());
    assertEquals(domain.getCpf().valor(), entity.getCpf());
    assertEquals(domain.getEmail().value(), entity.getEmail());
    assertEquals(domain.getEspecialidade(), entity.getEspecialidade());
    assertEquals(domain.getPassword(), entity.getPassword());
    assertTrue(entity.isAtivo());
  }

  @Test
  void toEntity_ShouldReturnNullWhenDomainIsNull() {
    assertNull(mapper.toEntity(null));
  }

  @Test
  void toDomain_ShouldReturnNullWhenEntityIsNull() {
    assertNull(mapper.toDomain(null));
  }
}
