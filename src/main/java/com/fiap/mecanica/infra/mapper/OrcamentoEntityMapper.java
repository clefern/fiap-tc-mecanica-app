package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.infra.entity.OrcamentoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrcamentoEntityMapper {
  Orcamento toDomain(OrcamentoEntity entity);

  OrcamentoEntity toEntity(Orcamento domain);
}
