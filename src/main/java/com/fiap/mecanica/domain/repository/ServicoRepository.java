package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.Servico;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicoRepository extends BaseRepository {
  Servico save(Servico servico);

  Optional<Servico> findById(UUID id);

  Page<Servico> findAll(Pageable pageable);

  Page<Servico> findByAtivoTrue(Pageable pageable);

  void delete(UUID id);
}
