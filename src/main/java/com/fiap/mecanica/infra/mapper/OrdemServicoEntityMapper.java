package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.entity.ItemOrdemServicoEntity;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrdemServicoEntityMapper {

  OrdemServico toDomain(OrdemServicoEntity entity);

  default OrdemServicoEntity toEntity(OrdemServico domain) {
    OrdemServicoEntity entity = toEntityScalars(domain);
    if (domain.getItens() != null) {
      List<ItemOrdemServicoEntity> itens = new ArrayList<>(domain.getItens().size());
      for (ItemOrdemServico item : domain.getItens()) {
        ItemOrdemServicoEntity itemEntity = toEntityItem(item);
        itemEntity.setOrdemServico(entity);
        itens.add(itemEntity);
      }
      entity.setItens(itens);
    }
    return entity;
  }

  @Named("toEntityScalars")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "itens", ignore = true)
  OrdemServicoEntity toEntityScalars(OrdemServico domain);

  ItemOrdemServico toDomainItem(ItemOrdemServicoEntity entity);

  @Mapping(target = "ordemServico", ignore = true)
  ItemOrdemServicoEntity toEntityItem(ItemOrdemServico domain);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "itens", ignore = true)
  void updateEntity(@MappingTarget OrdemServicoEntity entity, OrdemServico domain);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "ordemServico", ignore = true)
  void updateItem(@MappingTarget ItemOrdemServicoEntity entity, ItemOrdemServico domain);
}
