package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.RevokedTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRevokedTokenRepository extends JpaRepository<RevokedTokenEntity, Long> {
  Optional<RevokedTokenEntity> findByToken(String token);

  boolean existsByToken(String token);
}
