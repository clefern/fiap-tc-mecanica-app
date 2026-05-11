package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.PasswordResetTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPasswordResetTokenRepository
    extends JpaRepository<PasswordResetTokenEntity, UUID> {
  Optional<PasswordResetTokenEntity> findByToken(String token);
}
