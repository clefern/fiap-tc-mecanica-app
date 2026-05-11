package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.Atendente;
import com.fiap.mecanica.domain.valueobject.CPF;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AtendenteRepository {

  Optional<Atendente> findById(UUID id);

  Optional<Atendente> findByCpf(CPF cpf);

  Page<Atendente> findAll(Pageable pageable);

  boolean existsByCpf(CPF cpf);

  Atendente save(Atendente atendente);

  void deleteById(UUID id);
}
