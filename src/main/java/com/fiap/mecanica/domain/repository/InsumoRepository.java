package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.Insumo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InsumoRepository {
  Insumo save(Insumo insumo);

  Optional<Insumo> findById(UUID id);

  Page<Insumo> findAll(Pageable pageable);

  Page<Insumo> findByAtivoTrue(Pageable pageable);

  Page<Insumo> search(String termo, Pageable pageable);

  List<Insumo> findItensComEstoqueBaixo();

  void delete(UUID id);
}
