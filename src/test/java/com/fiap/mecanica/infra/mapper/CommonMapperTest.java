package com.fiap.mecanica.infra.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.valueobject.CNPJ;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommonMapperTest {

  private CommonMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new CommonMapper() {};
  }

  @Test
  @DisplayName("Should map CPF to String")
  void shouldMapCpfToString() {
    CPF cpf = CPF.of("52998224725");
    assertThat(mapper.map(cpf)).isEqualTo(cpf.valor());
    assertThat(mapper.map((CPF) null)).isNull();
  }

  @Test
  @DisplayName("Should map String to CPF")
  void shouldMapStringToCpf() {
    CPF cpf = mapper.mapCpf("52998224725");
    assertThat(cpf).isNotNull();
    assertThat(cpf.valor()).isEqualTo("52998224725");
    assertThat(mapper.mapCpf(null)).isNull();
  }

  @Test
  @DisplayName("Should map CNPJ to String")
  void shouldMapCnpjToString() {
    CNPJ cnpj = CNPJ.of("27865757000102");
    assertThat(mapper.map(cnpj)).isEqualTo(cnpj.valor());
    assertThat(mapper.map((CNPJ) null)).isNull();
  }

  @Test
  @DisplayName("Should map String to CNPJ")
  void shouldMapStringToCnpj() {
    CNPJ cnpj = mapper.mapCnpj("27865757000102");
    assertThat(cnpj).isNotNull();
    assertThat(cnpj.valor()).isEqualTo("27865757000102");
    assertThat(mapper.mapCnpj(null)).isNull();
  }

  @Test
  @DisplayName("Should map Email to String")
  void shouldMapEmailToString() {
    Email email = Email.of("test@example.com");
    assertThat(mapper.map(email)).isEqualTo("test@example.com");
    assertThat(mapper.map((Email) null)).isNull();
  }

  @Test
  @DisplayName("Should map String to Email")
  void shouldMapStringToEmail() {
    Email email = mapper.mapEmail("test@example.com");
    assertThat(email).isNotNull();
    assertThat(email.value()).isEqualTo("test@example.com");
    assertThat(mapper.mapEmail(null)).isNull();
  }

  @Test
  @DisplayName("Should map TelefoneBr to String")
  void shouldMapTelefoneBrToString() {
    TelefoneBr telefone = TelefoneBr.of("11912345678");
    assertThat(mapper.map(telefone)).isEqualTo("11912345678");
    assertThat(mapper.map((TelefoneBr) null)).isNull();
  }

  @Test
  @DisplayName("Should map String to TelefoneBr")
  void shouldMapStringToTelefoneBr() {
    TelefoneBr telefone = mapper.mapTelefone("11912345678");
    assertThat(telefone).isNotNull();
    assertThat(telefone.value()).isEqualTo("11912345678");
    assertThat(mapper.mapTelefone(null)).isNull();
  }

  @Test
  @DisplayName("Should map Endereco to String")
  void shouldMapEnderecoToString() {
    Endereco endereco = Endereco.of("Rua Teste, 123");
    assertThat(mapper.map(endereco)).isEqualTo("Rua Teste, 123");
    assertThat(mapper.map((Endereco) null)).isNull();
  }

  @Test
  @DisplayName("Should map String to Endereco")
  void shouldMapStringToEndereco() {
    Endereco endereco = mapper.mapEndereco("Rua Teste, 123");
    assertThat(endereco).isNotNull();
    assertThat(endereco.value()).isEqualTo("Rua Teste, 123");
    assertThat(mapper.mapEndereco(null)).isNull();
  }

  @Test
  @DisplayName("Should map PlacaVeiculo to String")
  void shouldMapPlacaVeiculoToString() {
    PlacaVeiculo placa = PlacaVeiculo.of("ABC1D23");
    assertThat(mapper.map(placa)).isEqualTo("ABC1D23");
    assertThat(mapper.map((PlacaVeiculo) null)).isNull();
  }

  @Test
  @DisplayName("Should map String to PlacaVeiculo")
  void shouldMapStringToPlacaVeiculo() {
    PlacaVeiculo placa = mapper.mapPlaca("ABC1D23");
    assertThat(placa).isNotNull();
    assertThat(placa.value()).isEqualTo("ABC1D23");
    assertThat(mapper.mapPlaca(null)).isNull();
  }
}
