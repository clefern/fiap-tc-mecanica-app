package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Servico;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicoService {
  Servico create(Servico servico);

  Servico update(UUID id, Servico servico);

  Optional<Servico> getById(UUID id);

  Page<Servico> getAll(Pageable pageable);

  Page<Servico> getAllAtivos(Pageable pageable);

  void delete(UUID id);
}
