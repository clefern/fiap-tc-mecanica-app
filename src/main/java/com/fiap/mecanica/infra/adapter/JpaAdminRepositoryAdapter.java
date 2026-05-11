package com.fiap.mecanica.infra.adapter;

import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.domain.repository.AdminRepository;
import com.fiap.mecanica.infra.entity.AdminEntity;
import com.fiap.mecanica.infra.jpa.JpaAdminRepository;
import com.fiap.mecanica.infra.mapper.AdminEntityMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAdminRepositoryAdapter implements AdminRepository {

  private final JpaAdminRepository jpaRepository;
  private final AdminEntityMapper mapper;

  public JpaAdminRepositoryAdapter(JpaAdminRepository jpaRepository, AdminEntityMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Admin save(Admin admin) {
    AdminEntity entity = mapper.toEntity(admin);
    AdminEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }
}
