package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.entity.ItemOrdemServicoEntity;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrdemServicoEntityMapper {

  OrdemServico toDomain(OrdemServicoEntity entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "itens", ignore = true) // Managed by @AfterMapping
  OrdemServicoEntity toEntity(OrdemServico domain);

  ItemOrdemServico toDomainItem(ItemOrdemServicoEntity entity);

  @Mapping(target = "ordemServico", ignore = true)
  ItemOrdemServicoEntity toEntityItem(ItemOrdemServico domain);

  @AfterMapping
  default void linkItens(@MappingTarget OrdemServicoEntity entity, OrdemServico domain) {
    if (domain.getItens() != null) {
      List<ItemOrdemServicoEntity> itens =
          domain.getItens().stream()
              .map(
                  item -> {
                    ItemOrdemServicoEntity itemEntity = toEntityItem(item);
                    itemEntity.setOrdemServico(entity);
                    return itemEntity;
                  })
              .collect(java.util.stream.Collectors.toList());
      entity.setItens(itens);
    }
  }
}
