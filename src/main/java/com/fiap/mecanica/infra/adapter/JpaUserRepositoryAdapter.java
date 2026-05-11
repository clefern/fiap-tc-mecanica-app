package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.model.*;
import com.fiap.mecanica.domain.repository.UserRepository;
import com.fiap.mecanica.domain.valueobject.*;
import com.fiap.mecanica.infra.entity.*;
import com.fiap.mecanica.infra.jpa.JpaUserRepository;
import com.fiap.mecanica.infra.mapper.UserEntityMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;
  private final UserEntityMapper mapper;

  public JpaUserRepositoryAdapter(JpaUserRepository jpaUserRepository, UserEntityMapper mapper) {
    this.jpaUserRepository = jpaUserRepository;
    this.mapper = mapper;
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return jpaUserRepository.findByEmail(email.value()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return jpaUserRepository.existsByEmail(email.value());
  }

  @Override
  public User save(User user) {
    UserEntity entity = mapper.toEntity(user);
    UserEntity saved = jpaUserRepository.save(entity);
    return mapper.toDomain(saved);
  }
}
