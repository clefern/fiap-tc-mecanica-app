package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Documento;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.VeiculoEntity;
import org.hibernate.Hibernate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = "spring",
    uses = {CommonMapper.class, VeiculoEntityMapper.class})
public abstract class ClienteEntityMapper {

  @Autowired protected VeiculoEntityMapper veiculoMapper;

  @Mapping(target = "documento", expression = "java(mapDocumento(entity))")
  @Mapping(target = "tipo", source = "tipoPessoa")
  @Mapping(target = "veiculosPorPlaca", ignore = true)
  @Mapping(target = "veiculos", ignore = true)
  public abstract Cliente toDomain(ClienteEntity entity);

  @Mapping(target = "documento", expression = "java(domain.getDocumento().valor())")
  @Mapping(target = "tipoPessoa", source = "tipo")
  @Mapping(target = "veiculos", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "lastLogin", ignore = true)
  public abstract ClienteEntity toEntity(Cliente domain);

  protected Documento mapDocumento(ClienteEntity entity) {
    if (entity.getTipoPessoa() == null) {
      return null;
    }
    TipoPessoa tipo = TipoPessoa.valueOf(entity.getTipoPessoa());
    if (tipo == TipoPessoa.FISICA) {
      return CPF.of(entity.getDocumento());
    } else {
      return CNPJ.of(entity.getDocumento());
    }
  }

  @AfterMapping
  protected void afterToDomain(ClienteEntity entity, @MappingTarget Cliente cliente) {
    if (entity.getVeiculos() != null && Hibernate.isInitialized(entity.getVeiculos())) {
      entity
          .getVeiculos()
          .forEach(
              ve -> {
                Veiculo v = veiculoMapper.toDomain(ve);
                if (v != null) {
                  cliente.adicionarVeiculo(v);
                }
              });
    }
  }

  @AfterMapping
  protected void afterToEntity(Cliente domain, @MappingTarget ClienteEntity entity) {
    domain
        .getVeiculos()
        .forEach(
            v -> {
              VeiculoEntity ve = veiculoMapper.toEntity(v);
              if (ve != null) {
                ve.setCliente(entity);
                entity.getVeiculos().add(ve);
              }
            });
  }
}
