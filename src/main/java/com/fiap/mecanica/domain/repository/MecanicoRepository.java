package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.Mecanico;
import com.fiap.mecanica.domain.valueobject.CPF;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MecanicoRepository {

  Optional<Mecanico> findById(UUID id);

  Optional<Mecanico> findByCpf(CPF cpf);

  Page<Mecanico> findAll(Pageable pageable);

  boolean existsByCpf(CPF cpf);

  Mecanico save(Mecanico mecanico);

  void deleteById(UUID id);
}
