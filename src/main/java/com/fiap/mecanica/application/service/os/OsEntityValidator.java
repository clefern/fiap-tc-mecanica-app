package com.fiap.mecanica.application.service.os;

import com.fiap.mecanica.domain.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.domain.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OsEntityValidator {

  private final ClienteRepository clienteRepository;
  private final VeiculoRepository veiculoRepository;

  public OsEntityValidator(
      ClienteRepository clienteRepository, VeiculoRepository veiculoRepository) {
    this.clienteRepository = clienteRepository;
    this.veiculoRepository = veiculoRepository;
  }

  public void validar(UUID clienteId, UUID veiculoId) {
    if (!clienteRepository.existsById(clienteId)) {
      throw new ClienteNaoEncontradoException(clienteId);
    }

    if (!veiculoRepository.existsByIdAndClienteId(veiculoId, clienteId)) {
      throw new VeiculoNaoEncontradoException(
          "Veículo não encontrado ou não pertence ao cliente. ID: " + veiculoId);
    }
  }
}
