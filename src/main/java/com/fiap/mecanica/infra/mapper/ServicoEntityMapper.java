package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Servico;
import com.fiap.mecanica.infra.entity.ServicoEntity;
import java.time.Duration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ServicoEntityMapper {

  default Servico toDomain(ServicoEntity entity) {
    if (entity == null) {
      return null;
    }
    return new Servico(
        entity.getId(),
        entity.getNome(),
        entity.getDescricao(),
        entity.getPrecoBase(),
        entity.isAtivo(),
        minutesToDuration(entity.getTempoEstimadoMinutos()),
        entity.getCategoria());
  }

  @Mapping(
      target = "tempoEstimadoMinutos",
      source = "tempoEstimado",
      qualifiedByName = "durationToMinutes")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  ServicoEntity toEntity(Servico domain);

  @Named("minutesToDuration")
  default Duration minutesToDuration(Long minutes) {
    return minutes != null ? Duration.ofMinutes(minutes) : null;
  }

  @Named("durationToMinutes")
  default Long durationToMinutes(Duration duration) {
    return duration != null ? duration.toMinutes() : null;
  }
}
