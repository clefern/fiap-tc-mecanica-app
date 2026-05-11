package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.ItemComercialEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaItemComercialRepository extends JpaRepository<ItemComercialEntity, UUID> {
  List<ItemComercialEntity> findByAtivoTrue();
}
