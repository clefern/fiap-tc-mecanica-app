package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.infra.entity.MecanicoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CommonMapper.class)
public interface MecanicoEntityMapper {
  Mecanico toDomain(MecanicoEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "lastLogin", ignore = true)
  MecanicoEntity toEntity(Mecanico domain);
}
