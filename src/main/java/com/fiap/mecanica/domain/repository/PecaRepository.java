package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.Peca;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PecaRepository extends BaseRepository {
  Peca save(Peca peca);

  Optional<Peca> findById(UUID id);

  Page<Peca> findAll(Pageable pageable);

  Page<Peca> findByAtivoTrue(Pageable pageable);

  Page<Peca> search(String termo, Pageable pageable);

  List<Peca> findItensComEstoqueBaixo();

  void delete(UUID id);
}
