package com.fiap.mecanica.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DtoTest {

  @Test
  @DisplayName("Deve cobrir ClienteRequest")
  void shouldCoverClienteRequest() {
    ClienteRequest dto = new ClienteRequest();
    dto.setNome("João");
    dto.setDocumento("12345678901");
    dto.setTipoPessoa(TipoPessoa.FISICA);
    dto.setEmail("joao@test.com");
    dto.setTelefone("11999999999");
    dto.setEndereco("Rua A");

    assertThat(dto.getNome()).isEqualTo("João");
    assertThat(dto.getDocumento()).isEqualTo("12345678901");
    assertThat(dto.getTipoPessoa()).isEqualTo(TipoPessoa.FISICA);
    assertThat(dto.getEmail()).isEqualTo("joao@test.com");
    assertThat(dto.getTelefone()).isEqualTo("11999999999");
    assertThat(dto.getEndereco()).isEqualTo("Rua A");
    assertThat(dto.toString()).contains("João");

    ClienteRequest dto2 =
        new ClienteRequest(
            "João", "12345678901", TipoPessoa.FISICA, "joao@test.com", "11999999999", "Rua A");
    assertThat(dto2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir ClienteResponse")
  void shouldCoverClienteResponse() {
    ClienteResponse dto = new ClienteResponse();
    UUID id = UUID.randomUUID();
    dto.setId(id);
    dto.setNome("João");
    dto.setDocumento("12345678901");
    dto.setTipoPessoa(TipoPessoa.FISICA);
    dto.setEmail("joao@test.com");
    dto.setTelefone("11999999999");
    dto.setEndereco("Rua A");

    assertThat(dto.getId()).isEqualTo(id);
    assertThat(dto.getNome()).isEqualTo("João");
    assertThat(dto.getDocumento()).isEqualTo("12345678901");
    assertThat(dto.getTipoPessoa()).isEqualTo(TipoPessoa.FISICA);
    assertThat(dto.getEmail()).isEqualTo("joao@test.com");
    assertThat(dto.getTelefone()).isEqualTo("11999999999");
    assertThat(dto.getEndereco()).isEqualTo("Rua A");
    assertThat(dto.toString()).contains("João");

    ClienteResponse dto2 =
        new ClienteResponse(
            id, "João", "12345678901", TipoPessoa.FISICA, "joao@test.com", "11999999999", "Rua A");
    assertThat(dto2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir VeiculoRequest")
  void shouldCoverVeiculoRequest() {
    VeiculoRequest dto = new VeiculoRequest();
    dto.setPlaca("ABC1234");
    dto.setModelo("Uno");
    dto.setMarca("Fiat");
    dto.setAno(2020);

    assertThat(dto.getPlaca()).isEqualTo("ABC1234");
    assertThat(dto.getModelo()).isEqualTo("Uno");
    assertThat(dto.getMarca()).isEqualTo("Fiat");
    assertThat(dto.getAno()).isEqualTo(2020);
    assertThat(dto.toString()).contains("Uno");

    VeiculoRequest dto2 = new VeiculoRequest("ABC1234", "Uno", "Fiat", 2020);
    assertThat(dto2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir VeiculoResponse")
  void shouldCoverVeiculoResponse() {
    VeiculoResponse dto = new VeiculoResponse();
    UUID id = UUID.randomUUID();
    dto.setId(id);
    dto.setPlaca("ABC1234");
    dto.setModelo("Uno");
    dto.setMarca("Fiat");
    dto.setAno(2020);

    assertThat(dto.getId()).isEqualTo(id);
    assertThat(dto.getPlaca()).isEqualTo("ABC1234");
    assertThat(dto.getModelo()).isEqualTo("Uno");
    assertThat(dto.getMarca()).isEqualTo("Fiat");
    assertThat(dto.getAno()).isEqualTo(2020);
    assertThat(dto.toString()).contains("Uno");

    VeiculoResponse dto2 = new VeiculoResponse(id, "ABC1234", "Fiat", "Uno", 2020);
    assertThat(dto2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir MecanicoRequest")
  void shouldCoverMecanicoRequest() {
    MecanicoRequest dto = new MecanicoRequest();
    dto.setNome("Mecânico");
    dto.setCpf("12345678901");
    dto.setEmail("mec@test.com");
    dto.setEspecialidade("Motor");

    assertThat(dto.getNome()).isEqualTo("Mecânico");
    assertThat(dto.getCpf()).isEqualTo("12345678901");
    assertThat(dto.getEmail()).isEqualTo("mec@test.com");
    assertThat(dto.getEspecialidade()).isEqualTo("Motor");
    assertThat(dto.toString()).contains("Mecânico");

    MecanicoRequest dto2 = new MecanicoRequest("Mecânico", "12345678901", "mec@test.com", "Motor");
    assertThat(dto2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir MecanicoResponse")
  void shouldCoverMecanicoResponse() {
    MecanicoResponse dto = new MecanicoResponse();
    UUID id = UUID.randomUUID();
    dto.setId(id);
    dto.setNome("Mecânico");
    dto.setCpf("12345678901");
    dto.setEmail("mec@test.com");
    dto.setEspecialidade("Motor");
    dto.setAtivo(true);

    assertThat(dto.getId()).isEqualTo(id);
    assertThat(dto.getNome()).isEqualTo("Mecânico");
    assertThat(dto.getCpf()).isEqualTo("12345678901");
    assertThat(dto.getEmail()).isEqualTo("mec@test.com");
    assertThat(dto.getEspecialidade()).isEqualTo("Motor");
    assertThat(dto.isAtivo()).isTrue();
    assertThat(dto.toString()).contains("Mecânico");

    MecanicoResponse dto2 =
        new MecanicoResponse(id, "Mecânico", "12345678901", "mec@test.com", "Motor", true);
    assertThat(dto2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir AtendenteRequest")
  void shouldCoverAtendenteRequest() {
    AtendenteRequest dto = new AtendenteRequest();
    dto.setNome("Atendente");
    dto.setCpf("12345678901");
    dto.setEmail("atd@test.com");
    dto.setMatricula("MAT-123");

    assertThat(dto.getNome()).isEqualTo("Atendente");
    assertThat(dto.getCpf()).isEqualTo("12345678901");
    assertThat(dto.getEmail()).isEqualTo("atd@test.com");
    assertThat(dto.getMatricula()).isEqualTo("MAT-123");
    assertThat(dto.toString()).contains("Atendente");

    AtendenteRequest dto2 =
        new AtendenteRequest("Atendente", "12345678901", "atd@test.com", "MAT-123");
    assertThat(dto2).isNotNull();
  }

  @Test
  @DisplayName("Deve cobrir AtendenteResponse")
  void shouldCoverAtendenteResponse() {
    AtendenteResponse dto = new AtendenteResponse();
    UUID id = UUID.randomUUID();
    dto.setId(id);
    dto.setNome("Atendente");
    dto.setCpf("12345678901");
    dto.setEmail("atd@test.com");
    dto.setAtivo(true);

    assertThat(dto.getId()).isEqualTo(id);
    assertThat(dto.getNome()).isEqualTo("Atendente");
    assertThat(dto.getCpf()).isEqualTo("12345678901");
    assertThat(dto.getEmail()).isEqualTo("atd@test.com");
    assertThat(dto.isAtivo()).isTrue();
    assertThat(dto.toString()).contains("Atendente");

    AtendenteResponse dto2 =
        new AtendenteResponse(id, "Atendente", "12345678901", "atd@test.com", true);
    assertThat(dto2).isNotNull();
  }
}
