package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Cliente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {
  Cliente create(Cliente novo);

  Cliente update(UUID id, Cliente atualizado);

  Optional<Cliente> getById(UUID id);

  Optional<Cliente> getByDocumento(String documento);

  Page<Cliente> getAll(Pageable pageable);

  void delete(UUID id);
}
