package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Mecanico;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MecanicoService {
  Mecanico create(Mecanico mecanico);

  Mecanico update(UUID id, Mecanico mecanico);

  Optional<Mecanico> getById(UUID id);

  Optional<Mecanico> getByCpf(String cpf);

  Page<Mecanico> getAll(Pageable pageable);

  void delete(UUID id);
}
