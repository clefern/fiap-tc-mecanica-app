package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.domain.valueobject.Email;
import java.util.Optional;

public interface UserRepository {
  Optional<User> findByEmail(Email email);

  boolean existsByEmail(Email email);

  User save(User user);
}
