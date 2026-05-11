package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.infra.entity.InsumoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InsumoEntityMapper {
  Insumo toDomain(InsumoEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  InsumoEntity toEntity(Insumo domain);
}
