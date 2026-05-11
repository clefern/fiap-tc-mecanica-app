package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrdemServicoRepository extends JpaRepository<OrdemServicoEntity, UUID> {

  @Override
  @EntityGraph(attributePaths = {"itens"})
  @NonNull Page<OrdemServicoEntity> findAll(@NonNull Pageable pageable);

  List<OrdemServicoEntity> findByStatusAndDataFechamentoBetweenAndMecanicoExecucaoIdIsNotNull(
      StatusOS status, LocalDateTime start, LocalDateTime end);

  @EntityGraph(attributePaths = {"itens"})
  Page<OrdemServicoEntity> findByStatusOrderByPrioridadeDescCreatedAtAsc(
      StatusOS status, Pageable pageable);

  @EntityGraph(attributePaths = {"itens"})
  Page<OrdemServicoEntity> findByStatusOrderByPrioridadeDescDataAprovacaoAsc(
      StatusOS status, Pageable pageable);

  @EntityGraph(attributePaths = {"itens"})
  Optional<OrdemServicoEntity> findWithItensById(UUID id);

  List<OrdemServicoEntity> findByStatusAndDataAprovacaoIsNotNullAndDataFechamentoIsNotNull(
      StatusOS status);

  @Query(
      value =
          """
          SELECT * FROM public.ordens_servico
          WHERE status NOT IN ('FINALIZADA', 'ENTREGUE', 'CANCELADA')
          ORDER BY
            CASE status
              WHEN 'EM_EXECUCAO'          THEN 1
              WHEN 'AGUARDANDO_APROVACAO' THEN 2
              WHEN 'APROVADA'             THEN 3
              WHEN 'EM_DIAGNOSTICO'       THEN 4
              WHEN 'RECEBIDA'             THEN 5
              ELSE 6
            END ASC,
            data_entrada ASC
          """,
      countQuery =
          "SELECT count(*) FROM public.ordens_servico WHERE status NOT IN ('FINALIZADA',"
              + " 'ENTREGUE', 'CANCELADA')",
      nativeQuery = true)
  Page<OrdemServicoEntity> listarFilaOperacional(Pageable pageable);

  Optional<OrdemServicoEntity> findByCodigo(String codigo);

  @EntityGraph(attributePaths = {"itens"})
  Page<OrdemServicoEntity> findByStatus(StatusOS status, Pageable pageable);

  @EntityGraph(attributePaths = {"itens"})
  Page<OrdemServicoEntity> findByClienteId(UUID clienteId, Pageable pageable);

  @EntityGraph(attributePaths = {"itens"})
  Page<OrdemServicoEntity> findByStatusAndClienteId(
      StatusOS status, UUID clienteId, Pageable pageable);
}
