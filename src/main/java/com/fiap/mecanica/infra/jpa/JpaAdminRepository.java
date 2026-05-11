package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.AdminEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAdminRepository extends JpaRepository<AdminEntity, UUID> {}
