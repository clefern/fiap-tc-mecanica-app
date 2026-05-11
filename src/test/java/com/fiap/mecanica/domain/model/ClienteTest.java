package com.fiap.mecanica.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.valueobject.*;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteTest {

  @Test
  @DisplayName("Should test Cliente domain model methods")
  void shouldTestClienteModel() {
    UUID id = UUID.randomUUID();
    Documento documento = CPF.of("12345678909");
    Email email = Email.of("test@test.com");
    TelefoneBr telefone = TelefoneBr.of("11999999999");
    Endereco endereco = Endereco.of("Rua A, 123, Bairro B, Cidade C - SP");
    TipoPessoa tipo = TipoPessoa.FISICA;

    Cliente cliente = new Cliente("Nome", documento, tipo, email, telefone, endereco);
    cliente.setId(id);
    cliente.setAtivo(true);

    assertThat(cliente.getId()).isEqualTo(id);
    assertThat(cliente.getNome()).isEqualTo("Nome");
    assertThat(cliente.getDocumento()).isEqualTo(documento);
    assertThat(cliente.getTipo()).isEqualTo(tipo);
    assertThat(cliente.getEmail()).isEqualTo(email);
    assertThat(cliente.getTelefone()).isEqualTo(telefone);
    assertThat(cliente.getEndereco()).isEqualTo(endereco);
    assertThat(cliente.isAtivo()).isTrue();

    // Test setters (update)
    cliente.setNome("Novo Nome");
    cliente.setEndereco(Endereco.of("Novo Endereço"));
    assertThat(cliente.getNome()).isEqualTo("Novo Nome");
    assertThat(cliente.getEndereco().value()).isEqualTo("Novo Endereço");

    // Test toString, equals, hashCode
    assertThat(cliente.toString()).contains("Novo Nome");

    Cliente cliente2 =
        new Cliente("Novo Nome", documento, tipo, email, telefone, Endereco.of("Novo Endereço"));
    cliente2.setId(id); // Same ID
    cliente2.setAtivo(true);

    assertThat(cliente).isEqualTo(cliente2);
    assertThat(cliente.hashCode()).isEqualTo(cliente2.hashCode());

    // Test veiculos management
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC1234"), "Modelo X", "Marca Y", 2020);

    cliente.adicionarVeiculo(veiculo);
    assertThat(cliente.getVeiculos()).hasSize(1);
    assertThat(cliente.getVeiculos().get(0)).isEqualTo(veiculo);

    cliente.removerVeiculo(PlacaVeiculo.of("ABC1234"));
    assertThat(cliente.getVeiculos()).isEmpty();
  }

  @Test
  @DisplayName("Should throw exception when creating Cliente with invalid data")
  void shouldThrowExceptionWhenCreatingWithInvalidData() {
    Documento doc = CPF.of("12345678909");
    Email email = Email.of("a@a.com");
    TelefoneBr tel = TelefoneBr.of("11999999999");
    Endereco end = Endereco.of("Rua A");
    TipoPessoa tipo = TipoPessoa.FISICA;

    assertThatThrownBy(() -> new Cliente(null, doc, tipo, email, tel, end))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nome é obrigatório");

    assertThatThrownBy(() -> new Cliente("", doc, tipo, email, tel, end))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nome é obrigatório");

    assertThatThrownBy(() -> new Cliente("Nome", null, tipo, email, tel, end))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Documento é obrigatório");

    assertThatThrownBy(() -> new Cliente("Nome", doc, null, email, tel, end))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Tipo de pessoa é obrigatório");

    assertThatThrownBy(() -> new Cliente("Nome", doc, tipo, null, tel, end))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email é obrigatório");

    assertThatThrownBy(() -> new Cliente("Nome", doc, tipo, email, null, end))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Telefone é obrigatório");

    assertThatThrownBy(() -> new Cliente("Nome", doc, tipo, email, tel, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Endereço é obrigatório");
  }

  @Test
  @DisplayName("Should throw exception when adding null vehicle")
  void shouldThrowExceptionWhenAddingNullVehicle() {
    Cliente c =
        new Cliente(
            "A",
            CPF.of("12345678909"),
            TipoPessoa.FISICA,
            Email.of("a@a.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("End"));
    assertThatThrownBy(() -> c.adicionarVeiculo(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Veículo não pode ser nulo");
  }

  @Test
  @DisplayName("Should throw exception when removing null vehicle")
  void shouldThrowExceptionWhenRemovingNullVehicle() {
    Cliente c =
        new Cliente(
            "A",
            CPF.of("12345678909"),
            TipoPessoa.FISICA,
            Email.of("a@a.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("End"));
    assertThatThrownBy(() -> c.removerVeiculo(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Placa não pode ser nula");
  }
}
