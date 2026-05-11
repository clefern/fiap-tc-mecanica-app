package com.fiap.mecanica.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.infra.entity.AtendenteEntity;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.MecanicoEntity;
import com.fiap.mecanica.infra.entity.UserEntity;
import com.fiap.mecanica.infra.jpa.JpaUserRepository;
import com.fiap.mecanica.infra.mapper.UserEntityMapper;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaUserRepositoryAdapterTest {

  @Mock private JpaUserRepository jpaUserRepository;

  @Mock private UserEntityMapper mapper;

  @InjectMocks private JpaUserRepositoryAdapter adapter;

  @Test
  @DisplayName("Deve encontrar Atendente por email")
  void shouldFindAtendenteByEmail() {
    AtendenteEntity entity = new AtendenteEntity();
    Atendente domain = mock(Atendente.class);

    when(jpaUserRepository.findByEmail("atendente@teste.com")).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<User> result = adapter.findByEmail(Email.of("atendente@teste.com"));

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(domain);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve encontrar Mecanico por email")
  void shouldFindMecanicoByEmail() {
    MecanicoEntity entity = new MecanicoEntity();
    Mecanico domain = mock(Mecanico.class);

    when(jpaUserRepository.findByEmail("mecanico@teste.com")).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<User> result = adapter.findByEmail(Email.of("mecanico@teste.com"));

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(domain);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve encontrar Cliente por email")
  void shouldFindClienteByEmail() {
    ClienteEntity entity = new ClienteEntity();
    Cliente domain = mock(Cliente.class);

    when(jpaUserRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Optional<User> result = adapter.findByEmail(Email.of("cliente@teste.com"));

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(domain);
    verify(mapper).toDomain(entity);
  }

  @Test
  @DisplayName("Deve retornar vazio quando email não existe")
  void shouldReturnEmptyWhenEmailNotFound() {
    when(jpaUserRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

    Optional<User> result = adapter.findByEmail(Email.of("inexistente@teste.com"));

    assertThat(result).isEmpty();
    verify(mapper, never()).toDomain(any());
  }

  @Test
  @DisplayName("Deve verificar se existe por email")
  void shouldCheckExistsByEmail() {
    when(jpaUserRepository.existsByEmail("teste@teste.com")).thenReturn(true);

    boolean exists = adapter.existsByEmail(Email.of("teste@teste.com"));

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Deve salvar usuário")
  void shouldSaveUser() {
    User domain = mock(User.class);
    UserEntity entity = mock(UserEntity.class);
    UserEntity savedEntity = mock(UserEntity.class);

    User savedDomain = mock(User.class);

    when(mapper.toEntity(domain)).thenReturn(entity);
    when(jpaUserRepository.save(entity)).thenReturn(savedEntity);
    when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

    User result = adapter.save(domain);

    assertThat(result).isEqualTo(savedDomain);
    verify(mapper).toEntity(domain);
    verify(jpaUserRepository).save(entity);
  }
}
