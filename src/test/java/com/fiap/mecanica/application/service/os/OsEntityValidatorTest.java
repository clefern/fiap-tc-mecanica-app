package com.fiap.mecanica.application.service.os;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.domain.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OsEntityValidatorTest {

  @Mock private ClienteRepository clienteRepository;
  @Mock private VeiculoRepository veiculoRepository;

  @InjectMocks private OsEntityValidator validator;

  @Test
  @DisplayName("Deve passar quando cliente e veículo existem")
  void devePassarQuandoClienteEVeiculoExistem() {
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    when(clienteRepository.existsById(clienteId)).thenReturn(true);
    when(veiculoRepository.existsByIdAndClienteId(veiculoId, clienteId)).thenReturn(true);

    assertThatCode(() -> validator.validar(clienteId, veiculoId)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Deve lançar ClienteNaoEncontradoException quando cliente não existe")
  void deveLancarExcecaoQuandoClienteNaoEncontrado() {
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    when(clienteRepository.existsById(clienteId)).thenReturn(false);

    assertThatThrownBy(() -> validator.validar(clienteId, veiculoId))
        .isInstanceOf(ClienteNaoEncontradoException.class);
  }

  @Test
  @DisplayName("Deve lançar VeiculoNaoEncontradoException quando veículo não pertence ao cliente")
  void deveLancarExcecaoQuandoVeiculoNaoPertenceAoCliente() {
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    when(clienteRepository.existsById(clienteId)).thenReturn(true);
    when(veiculoRepository.existsByIdAndClienteId(veiculoId, clienteId)).thenReturn(false);

    assertThatThrownBy(() -> validator.validar(clienteId, veiculoId))
        .isInstanceOf(VeiculoNaoEncontradoException.class)
        .hasMessageContaining(veiculoId.toString());
  }
}
