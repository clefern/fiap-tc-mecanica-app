package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Veiculo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VeiculoService {
  Veiculo create(UUID clienteId, Veiculo novo);

  Optional<Veiculo> getByPlaca(String placa);

  void deleteByPlaca(String placa);

  java.util.List<Veiculo> listByClienteId(UUID clienteId);

  Page<Veiculo> getAll(Pageable pageable);
}
