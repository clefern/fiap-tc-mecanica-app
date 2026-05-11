package com.fiap.mecanica.infra.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.entity.AdminEntity;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

class AdminEntityMapperTest {

  private AdminEntityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(AdminEntityMapper.class);
    ReflectionTestUtils.setField(mapper, "commonMapper", new CommonMapper() {});
  }

  @Test
  @DisplayName("Should map Entity to Domain")
  void shouldMapEntityToDomain() {
    UUID id = UUID.randomUUID();
    AdminEntity entity = new AdminEntity();
    entity.setId(id);
    entity.setNome("Admin Teste");
    entity.setEmail("admin@teste.com");
    entity.setPassword("senha123");
    entity.setRole(UserRole.ADMIN);
    entity.setAtivo(true);

    Admin domain = mapper.toDomain(entity);

    assertThat(domain).isNotNull();
    assertThat(domain.getId()).isEqualTo(id);
    assertThat(domain.getNome()).isEqualTo("Admin Teste");
    assertThat(domain.getEmail().value()).isEqualTo("admin@teste.com");
    assertThat(domain.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(domain.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Should map Domain to Entity")
  void shouldMapDomainToEntity() {
    UUID id = UUID.randomUUID();
    Admin domain = new Admin();
    domain.setId(id);
    domain.setNome("Admin Domain");
    domain.setEmail(Email.of("admin@domain.com"));
    domain.setPassword("senhaDomain");
    domain.setRole(UserRole.ADMIN);
    domain.setAtivo(true);

    AdminEntity entity = mapper.toEntity(domain);

    assertThat(entity).isNotNull();
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getNome()).isEqualTo("Admin Domain");
    assertThat(entity.getEmail()).isEqualTo("admin@domain.com");
    assertThat(entity.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(entity.isAtivo()).isTrue();
  }

  @Test
  @DisplayName("Should handle nulls")
  void shouldHandleNulls() {
    assertThat(mapper.toDomain(null)).isNull();
    assertThat(mapper.toEntity(null)).isNull();
  }
}
