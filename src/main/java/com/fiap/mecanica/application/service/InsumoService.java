package com.fiap.mecanica.application.service;

import com.fiap.mecanica.domain.model.Insumo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InsumoService {
  Insumo create(Insumo insumo);

  Insumo update(UUID id, Insumo insumo);

  Optional<Insumo> getById(UUID id);

  Page<Insumo> getAll(Pageable pageable);

  Page<Insumo> getAllAtivos(Pageable pageable);

  Page<Insumo> search(String termo, Pageable pageable);

  void delete(UUID id);

  Insumo registrarBaixaEstoque(UUID id, int quantidade);

  Insumo registrarEntradaEstoque(UUID id, int quantidade);

  Insumo atualizarParametrosEstoque(UUID id, Integer estoqueMinimo, Integer estoqueMaximo);
}
