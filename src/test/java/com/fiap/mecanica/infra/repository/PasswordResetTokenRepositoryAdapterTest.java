package com.fiap.mecanica.infra.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.model.PasswordResetToken;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.infra.entity.PasswordResetTokenEntity;
import com.fiap.mecanica.infra.entity.UserEntity;
import com.fiap.mecanica.infra.jpa.JpaPasswordResetTokenRepository;
import com.fiap.mecanica.infra.mapper.UserEntityMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenRepositoryAdapterTest {

  @Mock private JpaPasswordResetTokenRepository jpaRepository;

  @Mock private UserEntityMapper userMapper;

  @InjectMocks private PasswordResetTokenRepositoryAdapter adapter;

  @Test
  @DisplayName("Deve salvar token")
  void shouldSaveToken() {
    UUID id = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    User usuario = mock(User.class);
    UserEntity userEntity = mock(UserEntity.class);

    PasswordResetToken token = new PasswordResetToken(id, "token-123", usuario, now);
    PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
    entity.setId(id);

    when(userMapper.toEntity(usuario)).thenReturn(userEntity);
    when(jpaRepository.save(any(PasswordResetTokenEntity.class))).thenReturn(entity);
    when(userMapper.toDomain(any())).thenReturn(usuario);

    PasswordResetToken result = adapter.save(token);

    assertThat(result).isNotNull();
    verify(jpaRepository).save(any(PasswordResetTokenEntity.class));
  }

  @Test
  @DisplayName("Deve buscar por token")
  void shouldFindByToken() {
    String tokenStr = "token-123";
    PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
    entity.setToken(tokenStr);
    entity.setUser(mock(UserEntity.class));

    User usuario = mock(User.class);

    when(jpaRepository.findByToken(tokenStr)).thenReturn(Optional.of(entity));
    when(userMapper.toDomain(any())).thenReturn(usuario);

    Optional<PasswordResetToken> result = adapter.findByToken(tokenStr);

    assertThat(result).isPresent();
    assertThat(result.get().getToken()).isEqualTo(tokenStr);
  }

  @Test
  @DisplayName("Deve deletar token")
  void shouldDeleteToken() {
    UUID id = UUID.randomUUID();
    PasswordResetToken token =
        new PasswordResetToken(id, "token", mock(User.class), LocalDateTime.now());

    adapter.delete(token);

    verify(jpaRepository).deleteById(id);
  }

  @Test
  @DisplayName("Não deve deletar token quando ID for nulo")
  void shouldNotDeleteTokenWhenIdIsNull() {
    PasswordResetToken token =
        new PasswordResetToken(null, "token", mock(User.class), LocalDateTime.now());

    adapter.delete(token);

    verify(jpaRepository, org.mockito.Mockito.never()).deleteById(any());
  }

  @Test
  @DisplayName("Deve salvar novo token (ID nulo)")
  void shouldSaveNewToken() {
    UUID newId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    User usuario = mock(User.class);
    UserEntity userEntity = mock(UserEntity.class);

    // Token with null ID (new)
    PasswordResetToken token = new PasswordResetToken(null, "token-new", usuario, now);

    PasswordResetTokenEntity savedEntity = new PasswordResetTokenEntity();
    savedEntity.setId(newId);
    savedEntity.setToken("token-new");
    savedEntity.setUser(userEntity);
    savedEntity.setExpiryDate(now);

    when(userMapper.toEntity(usuario)).thenReturn(userEntity);
    when(jpaRepository.save(any(PasswordResetTokenEntity.class))).thenReturn(savedEntity);
    when(userMapper.toDomain(any())).thenReturn(usuario);

    PasswordResetToken result = adapter.save(token);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(newId);
    verify(jpaRepository).save(any(PasswordResetTokenEntity.class));
  }
}
