package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.PecaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPecaRepository extends JpaRepository<PecaEntity, UUID> {
  Page<PecaEntity> findByAtivoTrue(Pageable pageable);

  @Query(
      "SELECT p FROM PecaEntity p WHERE p.quantidadeEstoque <= p.estoqueMinimo AND p.ativo = true")
  List<PecaEntity> findEstoqueBaixo();

  @Query(
      "SELECT p FROM PecaEntity p "
          + "WHERE (lower(p.nome) LIKE lower(concat('%', :termo, '%')) "
          + "OR lower(p.descricao) LIKE lower(concat('%', :termo, '%')))"
          + " AND p.ativo = true")
  Page<PecaEntity> search(@Param("termo") String termo, Pageable pageable);
}
