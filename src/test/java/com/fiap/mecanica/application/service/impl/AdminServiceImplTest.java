package com.fiap.mecanica.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.domain.repository.AdminRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

  @Mock private AdminRepository adminRepository;

  @InjectMocks private AdminServiceImpl adminService;

  @Test
  @DisplayName("Deve criar admin com sucesso")
  void shouldCreateAdmin() {
    Admin admin = new Admin();
    admin.setNome("Admin Teste");

    when(adminRepository.save(admin)).thenReturn(admin);

    Admin result = adminService.create(admin);

    assertThat(result).isNotNull();
    assertThat(result.getNome()).isEqualTo("Admin Teste");
    verify(adminRepository).save(admin);
  }
}
