package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Port de repositório para Veiculo (Camada de Domínio). */
public interface VeiculoRepository extends BaseRepository {

  Optional<Veiculo> findById(UUID id);

  Optional<Veiculo> findByPlaca(PlacaVeiculo placa);

  boolean existsByPlaca(PlacaVeiculo placa);

  Page<Veiculo> findAll(Pageable pageable);

  /**
   * Persiste Veículo associado a um Cliente. Retorna a entidade de domínio (sem dependência de
   * tecnologias de infra).
   */
  Veiculo save(UUID clienteId, Veiculo veiculo);

  void deleteById(UUID id);

  /**
   * Remove veículo pela placa. Útil para operações na camada de apresentação quando o id técnico
   * não está disponível.
   */
  void deleteByPlaca(PlacaVeiculo placa);

  /** Verifica se um veículo existe e pertence ao cliente informado. */
  boolean existsByIdAndClienteId(UUID id, UUID clienteId);

  /** Lista veículos de um cliente (por clienteId técnico). */
  List<Veiculo> findAllByClienteId(UUID clienteId);
}
