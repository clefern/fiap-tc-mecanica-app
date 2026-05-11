package com.fiap.mecanica.presentation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.valueobject.*;
import com.fiap.mecanica.presentation.dto.ClienteRequest;
import com.fiap.mecanica.presentation.dto.ClienteResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClienteMapperTest {

  private final ClienteMapper mapper = new ClienteMapper();

  @Test
  void toDomain_ShouldMapCorrectly() {
    ClienteRequest request = new ClienteRequest();
    request.setNome("John Doe");
    request.setDocumento("12345678909");
    request.setTipoPessoa(TipoPessoa.FISICA);
    request.setEmail("john@example.com");
    request.setTelefone("11999999999");
    request.setEndereco("Rua Teste");

    Cliente cliente = mapper.toDomain(request);

    assertThat(cliente.getNome()).isEqualTo(request.getNome());
    assertThat(cliente.getDocumento().valor()).isEqualTo(request.getDocumento());
    assertThat(cliente.getTipo()).isEqualTo(request.getTipoPessoa());
    assertThat(cliente.getEmail().value()).isEqualTo(request.getEmail());
  }

  @Test
  void toDomain_ShouldMapJuridicaCorrectly() {
    ClienteRequest request = new ClienteRequest();
    request.setNome("Empresa Ltd");
    request.setDocumento("00000000000191");
    request.setTipoPessoa(TipoPessoa.JURIDICA);
    request.setEmail("empresa@example.com");
    request.setTelefone("11999999999");
    request.setEndereco("Rua Corp");

    Cliente cliente = mapper.toDomain(request);

    assertThat(cliente.getDocumento()).isInstanceOf(CNPJ.class);
    assertThat(cliente.getDocumento().valor()).isEqualTo("00000000000191");
  }

  @Test
  void toDomain_ShouldReturnNullWhenRequestIsNull() {
    assertThat(mapper.toDomain(null)).isNull();
  }

  @Test
  void toResponse_ShouldMapCorrectly() {
    Cliente cliente =
        new Cliente(
            "John Doe",
            CPF.of("12345678909"),
            TipoPessoa.FISICA,
            Email.of("john@example.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua Teste"));
    cliente.setId(UUID.randomUUID());

    ClienteResponse response = mapper.toResponse(cliente);

    assertThat(response.getId()).isEqualTo(cliente.getId());
    assertThat(response.getNome()).isEqualTo(cliente.getNome());
    assertThat(response.getDocumento()).isEqualTo(cliente.getDocumento().valor());
  }

  @Test
  void toResponse_ShouldReturnNullWhenClienteIsNull() {
    assertThat(mapper.toResponse(null)).isNull();
  }
}
