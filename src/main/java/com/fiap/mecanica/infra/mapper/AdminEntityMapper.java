package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.infra.entity.AdminEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CommonMapper.class)
public interface AdminEntityMapper {
  Admin toDomain(AdminEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "lastLogin", ignore = true)
  AdminEntity toEntity(Admin domain);
}
