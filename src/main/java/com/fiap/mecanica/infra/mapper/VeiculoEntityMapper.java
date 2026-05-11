package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.VeiculoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CommonMapper.class)
public interface VeiculoEntityMapper {

  Veiculo toDomain(VeiculoEntity entity);

  @Mapping(target = "cliente", ignore = true)
  VeiculoEntity toEntity(Veiculo domain);

  default VeiculoEntity toEntity(Veiculo domain, ClienteEntity cliente) {
    VeiculoEntity entity = toEntity(domain);
    if (entity != null) {
      entity.setCliente(cliente);
    }
    return entity;
  }
}
