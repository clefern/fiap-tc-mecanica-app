package com.fiap.mecanica.infra.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.model.User;
import com.fiap.mecanica.infra.entity.AtendenteEntity;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.MecanicoEntity;
import com.fiap.mecanica.infra.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserEntityMapperTest {

  @Mock private AtendenteEntityMapper atendenteMapper;
  @Mock private MecanicoEntityMapper mecanicoMapper;
  @Mock private ClienteEntityMapper clienteMapper;
  @Mock private AdminEntityMapper adminMapper;

  private UserEntityMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new UserEntityMapper(atendenteMapper, mecanicoMapper, clienteMapper, adminMapper);
  }

  @Test
  void toDomain_AtendenteEntity_ShouldDelegateToAtendenteMapper() {
    AtendenteEntity entity = new AtendenteEntity();
    Atendente expected = mock(Atendente.class);
    when(atendenteMapper.toDomain(entity)).thenReturn(expected);

    User result = mapper.toDomain(entity);

    assertEquals(expected, result);
    verify(atendenteMapper).toDomain(entity);
    verifyNoInteractions(mecanicoMapper, clienteMapper);
  }

  @Test
  void toDomain_MecanicoEntity_ShouldDelegateToMecanicoMapper() {
    MecanicoEntity entity = new MecanicoEntity();
    Mecanico expected = mock(Mecanico.class);
    when(mecanicoMapper.toDomain(entity)).thenReturn(expected);

    User result = mapper.toDomain(entity);

    assertEquals(expected, result);
    verify(mecanicoMapper).toDomain(entity);
    verifyNoInteractions(atendenteMapper, clienteMapper);
  }

  @Test
  void toDomain_ClienteEntity_ShouldDelegateToClienteMapper() {
    ClienteEntity entity = new ClienteEntity();
    Cliente expected = mock(Cliente.class);
    when(clienteMapper.toDomain(entity)).thenReturn(expected);

    User result = mapper.toDomain(entity);

    assertEquals(expected, result);
    verify(clienteMapper).toDomain(entity);
    verifyNoInteractions(atendenteMapper, mecanicoMapper);
  }

  @Test
  void toDomain_Null_ShouldReturnNull() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  void toDomain_UnknownEntity_ShouldThrowException() {
    UserEntity entity = mock(UserEntity.class);
    assertThrows(IllegalArgumentException.class, () -> mapper.toDomain(entity));
  }

  @Test
  void toEntity_Atendente_ShouldDelegateToAtendenteMapper() {
    Atendente domain = mock(Atendente.class);
    AtendenteEntity expected = new AtendenteEntity();
    when(atendenteMapper.toEntity(domain)).thenReturn(expected);

    UserEntity result = mapper.toEntity(domain);

    assertEquals(expected, result);
    verify(atendenteMapper).toEntity(domain);
    verifyNoInteractions(mecanicoMapper, clienteMapper);
  }

  @Test
  void toEntity_Mecanico_ShouldDelegateToMecanicoMapper() {
    Mecanico domain = mock(Mecanico.class);
    MecanicoEntity expected = new MecanicoEntity();
    when(mecanicoMapper.toEntity(domain)).thenReturn(expected);

    UserEntity result = mapper.toEntity(domain);

    assertEquals(expected, result);
    verify(mecanicoMapper).toEntity(domain);
    verifyNoInteractions(atendenteMapper, clienteMapper);
  }

  @Test
  void toEntity_Cliente_ShouldDelegateToClienteMapper() {
    Cliente domain = mock(Cliente.class);
    ClienteEntity expected = new ClienteEntity();
    when(clienteMapper.toEntity(domain)).thenReturn(expected);

    UserEntity result = mapper.toEntity(domain);

    assertEquals(expected, result);
    verify(clienteMapper).toEntity(domain);
    verifyNoInteractions(atendenteMapper, mecanicoMapper);
  }

  @Test
  void toEntity_Null_ShouldReturnNull() {
    assertNull(mapper.toEntity(null));
  }

  @Test
  void toEntity_UnknownDomain_ShouldThrowException() {
    User domain = mock(User.class);
    assertThrows(IllegalArgumentException.class, () -> mapper.toEntity(domain));
  }
}
