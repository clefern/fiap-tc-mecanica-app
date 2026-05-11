package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.infra.entity.AdminEntity;
import com.fiap.mecanica.infra.jpa.JpaAdminRepository;
import com.fiap.mecanica.infra.mapper.AdminEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaAdminRepositoryAdapterTest {

  @Mock private JpaAdminRepository jpaRepository;

  @Mock private AdminEntityMapper mapper;

  @InjectMocks private JpaAdminRepositoryAdapter adapter;

  @Test
  @DisplayName("Should save admin successfully")
  void shouldSaveAdmin() {
    Admin admin = new Admin();
    admin.setNome("Admin Teste");

    AdminEntity entity = new AdminEntity();
    entity.setNome("Admin Teste");

    when(mapper.toEntity(admin)).thenReturn(entity);
    when(jpaRepository.save(entity)).thenReturn(entity);
    when(mapper.toDomain(entity)).thenReturn(admin);

    Admin saved = adapter.save(admin);

    assertThat(saved).isNotNull();
    assertThat(saved.getNome()).isEqualTo("Admin Teste");
    verify(jpaRepository).save(entity);
    verify(mapper).toEntity(admin);
    verify(mapper).toDomain(entity);
  }
}
