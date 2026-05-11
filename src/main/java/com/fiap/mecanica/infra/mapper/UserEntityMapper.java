package com.fiap.mecanica.infra.mapper;

import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.infra.entity.AdminEntity;
import com.fiap.mecanica.infra.entity.AtendenteEntity;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.MecanicoEntity;
import com.fiap.mecanica.infra.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

  private final AtendenteEntityMapper atendenteMapper;
  private final MecanicoEntityMapper mecanicoMapper;
  private final ClienteEntityMapper clienteMapper;
  private final AdminEntityMapper adminMapper;

  public UserEntityMapper(
      AtendenteEntityMapper atendenteMapper,
      MecanicoEntityMapper mecanicoMapper,
      ClienteEntityMapper clienteMapper,
      AdminEntityMapper adminMapper) {
    this.atendenteMapper = atendenteMapper;
    this.mecanicoMapper = mecanicoMapper;
    this.clienteMapper = clienteMapper;
    this.adminMapper = adminMapper;
  }

  public User toDomain(UserEntity entity) {
    if (entity == null) return null;

    return switch (entity) {
      case AtendenteEntity ae -> atendenteMapper.toDomain(ae);
      case MecanicoEntity me -> mecanicoMapper.toDomain(me);
      case ClienteEntity ce -> clienteMapper.toDomain(ce);
      case AdminEntity ad -> adminMapper.toDomain(ad);
      default -> throw new IllegalArgumentException(
          "Unknown UserEntity type: " + entity.getClass().getName());
    };
  }

  public UserEntity toEntity(User domain) {
    if (domain == null) return null;

    return switch (domain) {
      case Atendente a -> atendenteMapper.toEntity(a);
      case Mecanico m -> mecanicoMapper.toEntity(m);
      case Cliente c -> clienteMapper.toEntity(c);
      case Admin ad -> adminMapper.toEntity(ad);
      default -> throw new IllegalArgumentException(
          "Unknown User domain type: " + domain.getClass().getName());
    };
  }
}
