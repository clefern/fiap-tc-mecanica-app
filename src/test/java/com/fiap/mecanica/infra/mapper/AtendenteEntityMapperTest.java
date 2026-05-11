package com.fiap.mecanica.infra.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.entity.AtendenteEntity;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

class AtendenteEntityMapperTest {

  private AtendenteEntityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(AtendenteEntityMapper.class);
    ReflectionTestUtils.setField(mapper, "commonMapper", Mappers.getMapper(CommonMapper.class));
  }

  @Test
  void toDomain() {
    AtendenteEntity entity = new AtendenteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Maria Silva");
    entity.setCpf("39053344705");
    entity.setEmail("maria@test.com");
    entity.setPassword("secret");
    entity.setAtivo(true);

    Atendente domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertEquals(entity.getId(), domain.getId());
    assertEquals(entity.getNome(), domain.getNome());
    assertEquals(entity.getCpf(), domain.getCpf().valor());
    assertEquals(entity.getEmail(), domain.getEmail().value());
    assertEquals(entity.getPassword(), domain.getPassword());
    assertTrue(domain.isAtivo());
  }

  @Test
  void toDomain_ShouldReturnNullWhenEntityIsNull() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  void toDomain_ShouldMapInactiveCorrectly() {
    AtendenteEntity entity = new AtendenteEntity();
    entity.setId(UUID.randomUUID());
    entity.setNome("Maria Silva");
    entity.setCpf("39053344705");
    entity.setEmail("maria@test.com");
    entity.setPassword("secret");
    entity.setAtivo(false);

    Atendente domain = mapper.toDomain(entity);

    assertNotNull(domain);
    assertFalse(domain.isAtivo());
  }

  @Test
  void toEntity() {
    Atendente domain =
        new Atendente("Maria Silva", CPF.of("39053344705"), Email.of("maria@test.com"));
    domain.setId(UUID.randomUUID());
    domain.setPassword("secret");
    domain.ativar();

    AtendenteEntity entity = mapper.toEntity(domain);

    assertNotNull(entity);
    assertEquals(domain.getId(), entity.getId());
    assertEquals(domain.getNome(), entity.getNome());
    assertEquals(domain.getCpf().valor(), entity.getCpf());
    assertEquals(domain.getEmail().value(), entity.getEmail());
    assertEquals(domain.getPassword(), entity.getPassword());
    assertTrue(entity.isAtivo());
  }

  @Test
  void toEntity_ShouldReturnNullWhenDomainIsNull() {
    assertNull(mapper.toEntity(null));
  }
}
