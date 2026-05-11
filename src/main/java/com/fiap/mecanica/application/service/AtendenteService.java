package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Atendente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AtendenteService {
  Atendente create(Atendente atendente);

  Atendente update(UUID id, Atendente atendente);

  Optional<Atendente> getById(UUID id);

  Optional<Atendente> getByCpf(String cpf);

  Page<Atendente> getAll(Pageable pageable);

  void delete(UUID id);
}
