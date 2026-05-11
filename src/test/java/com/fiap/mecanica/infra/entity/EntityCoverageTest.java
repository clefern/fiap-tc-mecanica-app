package com.fiap.mecanica.infra.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntityCoverageTest {

  @Test
  @DisplayName("Deve cobrir ClienteEntity e UserEntity")
  void shouldCoverClienteEntity() {
    ClienteEntity entity = new ClienteEntity();
    UUID id = UUID.randomUUID();
    entity.setId(id);
    entity.setNome("João");
    entity.setEmail("joao@test.com");
    entity.setPassword("hash");
    entity.setAtivo(true);
    entity.setRole(UserRole.CLIENTE);
    entity.setLastLogin(LocalDateTime.now());
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());
    entity.setDocumento("12345678901");
    entity.setTipoPessoa("FISICA");
    entity.setTelefone("11999999999");
    entity.setEndereco("Rua A");
    entity.setVeiculos(new ArrayList<>());

    // UserEntity abstract methods
    entity.onCreate();
    entity.onUpdate();

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getNome()).isEqualTo("João");
    assertThat(entity.getEmail()).isEqualTo("joao@test.com");
    assertThat(entity.getPassword()).isEqualTo("hash");
    assertThat(entity.isAtivo()).isTrue();
    assertThat(entity.getRole()).isEqualTo(UserRole.CLIENTE);
    assertThat(entity.getLastLogin()).isNotNull();
    assertThat(entity.getCreatedAt()).isNotNull();
    assertThat(entity.getUpdatedAt()).isNotNull();
    assertThat(entity.getDocumento()).isEqualTo("12345678901");
    assertThat(entity.getTipoPessoa()).isEqualTo("FISICA");
    assertThat(entity.getTelefone()).isEqualTo("11999999999");
    assertThat(entity.getEndereco()).isEqualTo("Rua A");
    assertThat(entity.getVeiculos()).isEmpty();
    assertThat(entity.toString()).contains("João");

    ClienteEntity entity2 =
        new ClienteEntity("12345678901", "FISICA", "11999999999", "Rua A", new ArrayList<>());
    assertThat(entity2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir ItemComercialEntity via PecaEntity")
  void shouldCoverItemComercialEntity() {
    PecaEntity entity = new PecaEntity();
    UUID id = UUID.randomUUID();

    entity.setId(id);
    entity.setNome("Peca Teste");
    entity.setDescricao("Descricao");
    entity.setPrecoBase(BigDecimal.TEN);
    entity.setAtivo(true);
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());

    // Abstract methods
    entity.onCreate();
    entity.onUpdate();

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getNome()).isEqualTo("Peca Teste");
    assertThat(entity.getDescricao()).isEqualTo("Descricao");
    assertThat(entity.getPrecoBase()).isEqualTo(BigDecimal.TEN);
    assertThat(entity.isAtivo()).isTrue();
    assertThat(entity.getCreatedAt()).isNotNull();
    assertThat(entity.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir PasswordResetTokenEntity")
  void shouldCoverPasswordResetTokenEntity() {
    PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
    UUID id = UUID.randomUUID();
    String token = "token-123";
    UserEntity user = new ClienteEntity();
    LocalDateTime expiry = LocalDateTime.now();

    entity.setId(id);
    entity.setToken(token);
    entity.setUser(user);
    entity.setExpiryDate(expiry);

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getToken()).isEqualTo(token);
    assertThat(entity.getUser()).isEqualTo(user);
    assertThat(entity.getExpiryDate()).isEqualTo(expiry);

    PasswordResetTokenEntity entity2 = new PasswordResetTokenEntity(token, user, expiry);
    assertThat(entity2.getToken()).isEqualTo(token);
    assertThat(entity2.getUser()).isEqualTo(user);
    assertThat(entity2.getExpiryDate()).isEqualTo(expiry);
  }

  @Test
  @DisplayName("Deve cobrir VeiculoEntity")
  void shouldCoverVeiculoEntity() {
    VeiculoEntity entity = new VeiculoEntity();
    UUID id = UUID.randomUUID();
    ClienteEntity cliente = new ClienteEntity();
    entity.setId(id);
    entity.setCliente(cliente);
    entity.setPlaca("ABC1234");
    entity.setMarca("Fiat");
    entity.setModelo("Uno");
    entity.setAno(2020);

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getCliente()).isEqualTo(cliente);
    assertThat(entity.getPlaca()).isEqualTo("ABC1234");
    assertThat(entity.getMarca()).isEqualTo("Fiat");
    assertThat(entity.getModelo()).isEqualTo("Uno");
    assertThat(entity.getAno()).isEqualTo(2020);
    assertThat(entity.toString()).contains("Uno");

    VeiculoEntity entity2 = new VeiculoEntity(id, cliente, "ABC1234", "Fiat", "Uno", 2020);
    assertThat(entity2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir MecanicoEntity")
  void shouldCoverMecanicoEntity() {
    MecanicoEntity entity = new MecanicoEntity();
    entity.setCpf("12345678901");
    entity.setEspecialidade("Motor");

    assertThat(entity.getCpf()).isEqualTo("12345678901");
    assertThat(entity.getEspecialidade()).isEqualTo("Motor");
    assertThat(entity.toString()).contains("Motor");

    MecanicoEntity entity2 = new MecanicoEntity("12345678901", "Motor");
    assertThat(entity2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir AtendenteEntity")
  void shouldCoverAtendenteEntity() {
    AtendenteEntity entity = new AtendenteEntity();
    assertThat(entity).isNotNull();
    // AtendenteEntity might be empty or inherit everything
    assertThat(entity.toString()).isNotNull();
  }
}
