package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Peca;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PecaService {
  Peca create(Peca peca);

  Peca update(UUID id, Peca peca);

  Optional<Peca> getById(UUID id);

  Page<Peca> getAll(Pageable pageable);

  Page<Peca> getAllAtivos(Pageable pageable);

  Page<Peca> search(String termo, Pageable pageable);

  void delete(UUID id);

  Peca registrarBaixaEstoque(UUID id, int quantidade);

  Peca registrarEntradaEstoque(UUID id, int quantidade);

  Peca atualizarParametrosEstoque(UUID id, Integer estoqueMinimo, Integer estoqueMaximo);
}
