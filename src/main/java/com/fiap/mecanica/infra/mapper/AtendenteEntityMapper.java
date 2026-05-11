package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.infra.entity.AtendenteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CommonMapper.class)
public interface AtendenteEntityMapper {
  Atendente toDomain(AtendenteEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "lastLogin", ignore = true)
  AtendenteEntity toEntity(Atendente domain);
}
