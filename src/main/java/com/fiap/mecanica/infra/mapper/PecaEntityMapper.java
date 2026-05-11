package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.infra.entity.PecaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PecaEntityMapper {
  Peca toDomain(PecaEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  PecaEntity toEntity(Peca domain);
}
