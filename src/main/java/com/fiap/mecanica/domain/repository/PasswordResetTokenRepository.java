package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.PasswordResetToken;
import java.util.Optional;

public interface PasswordResetTokenRepository extends BaseRepository {
  PasswordResetToken save(PasswordResetToken token);

  Optional<PasswordResetToken> findByToken(String token);

  void delete(PasswordResetToken token);
}
