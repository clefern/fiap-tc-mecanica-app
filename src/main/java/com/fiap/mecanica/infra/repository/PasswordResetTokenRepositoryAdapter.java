package com.fiap.mecanica.infra.repository;

import com.fiap.mecanica.domain.model.PasswordResetToken;
import com.fiap.mecanica.domain.repository.PasswordResetTokenRepository;
import com.fiap.mecanica.infra.entity.PasswordResetTokenEntity;
import com.fiap.mecanica.infra.jpa.JpaPasswordResetTokenRepository;
import com.fiap.mecanica.infra.mapper.UserEntityMapper;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

  private final JpaPasswordResetTokenRepository jpaRepository;
  private final UserEntityMapper userMapper;

  public PasswordResetTokenRepositoryAdapter(
      JpaPasswordResetTokenRepository jpaRepository, UserEntityMapper userMapper) {
    this.jpaRepository = jpaRepository;
    this.userMapper = userMapper;
  }

  @Override
  public PasswordResetToken save(PasswordResetToken token) {
    PasswordResetTokenEntity entity = toEntity(token);
    PasswordResetTokenEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<PasswordResetToken> findByToken(String token) {
    return jpaRepository.findByToken(token).map(this::toDomain);
  }

  @Override
  public void delete(PasswordResetToken token) {
    if (token.getId() != null) {
      jpaRepository.deleteById(token.getId());
    }
  }

  private PasswordResetTokenEntity toEntity(PasswordResetToken domain) {
    PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
    if (domain.getId() != null) {
      entity.setId(domain.getId());
    }
    entity.setToken(domain.getToken());
    entity.setUser(userMapper.toEntity(domain.getUser()));
    entity.setExpiryDate(domain.getExpiryDate());
    return entity;
  }

  private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
    return new PasswordResetToken(
        entity.getId(),
        entity.getToken(),
        userMapper.toDomain(entity.getUser()),
        entity.getExpiryDate());
  }
}
