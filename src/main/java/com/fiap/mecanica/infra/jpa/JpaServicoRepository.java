package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.ServicoEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaServicoRepository extends JpaRepository<ServicoEntity, UUID> {
  Page<ServicoEntity> findByAtivoTrue(Pageable pageable);
}
