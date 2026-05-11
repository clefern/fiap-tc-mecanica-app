package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class VeiculoRepositoryIntegrationTest {

  @MockBean private JavaMailSender javaMailSender;

  @Autowired ClienteRepository clienteRepository;

  @Autowired VeiculoRepository veiculoRepository;

  @Autowired JdbcTemplate jdbcTemplate;

  @Autowired EntityManager entityManager;

  private Cliente novoCliente(String nome, String cpf, String email) {
    return new Cliente(
        nome,
        CPF.of(cpf),
        TipoPessoa.FISICA,
        Email.of(email),
        TelefoneBr.of("11987654321"),
        Endereco.of("Rua A, 123"));
  }

  private Veiculo novoVeiculo(String placa, String marca, String modelo, int ano) {
    return new Veiculo(PlacaVeiculo.of(placa), modelo, marca, ano);
  }

  @Test
  @DisplayName("save deve vincular clienteId, permitir findByPlaca e existsByPlaca")
  void saveFindExistsFlow() {
    Cliente cliente = novoCliente("Cliente Veiculo", "39053344705", "cliente.veiculo@example.com");
    Cliente persistedCliente = clienteRepository.save(cliente);
    UUID clienteId = persistedCliente.getId();

    Veiculo veiculo = novoVeiculo("XYZ9C99", "Toyota", "Corolla", 2020);
    Veiculo saved = veiculoRepository.save(clienteId, veiculo);
    entityManager.flush();

    assertThat(veiculoRepository.existsByPlaca(veiculo.getPlaca())).isTrue();

    Optional<Veiculo> byPlaca = veiculoRepository.findByPlaca(veiculo.getPlaca());
    assertThat(byPlaca).isPresent();
    Veiculo loaded = byPlaca.get();
    assertThat(loaded.getMarca()).isEqualTo("Toyota");
    assertThat(loaded.getModelo()).isEqualTo("Corolla");
    assertThat(loaded.getAno()).isEqualTo(2020);
  }

  @Test
  @DisplayName("deleteById deve remover veículo e existsByPlaca retornar false")
  void deleteByIdRemovesVehicle() {
    Cliente cliente = novoCliente("Cliente Del", "15350946056", "cliente.del@example.com");
    Cliente persistedCliente = clienteRepository.save(cliente);
    UUID clienteId = persistedCliente.getId();

    Veiculo veiculo = novoVeiculo("BRA2E19", "Honda", "Civic", 2019);
    veiculoRepository.save(clienteId, veiculo);
    entityManager.flush();

    UUID veiculoId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM public.veiculos WHERE placa = ?",
            UUID.class,
            veiculo.getPlaca().value());
    veiculoRepository.deleteById(veiculoId);
    entityManager.flush();

    assertThat(veiculoRepository.existsByPlaca(veiculo.getPlaca())).isFalse();
    assertThat(veiculoRepository.findById(veiculoId)).isEmpty();
  }

  @Test
  @DisplayName("save com placa duplicada deve falhar (uk_veiculos_placa)")
  void saveDuplicatePlacaShouldFail() {
    Cliente cliente = novoCliente("Cliente Dup", "52998224725", "cliente.dup@example.com");
    Cliente persistedCliente = clienteRepository.save(cliente);
    UUID clienteId = persistedCliente.getId();

    // Usa uma placa exclusiva para este teste para evitar interferência entre métodos
    Veiculo v1 = novoVeiculo("ZZZ1Z99", "Ford", "Ka", 2018);
    Veiculo v2 = novoVeiculo("ZZZ1Z99", "Fiat", "Uno", 2015);
    veiculoRepository.save(clienteId, v1);
    entityManager.flush();

    assertThrows(
        PersistenceException.class,
        () -> {
          veiculoRepository.save(clienteId, v2);
          entityManager.flush();
        });
  }
}
