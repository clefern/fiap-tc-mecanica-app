package com.fiap.mecanica.infra.jpa;

import com.fiap.mecanica.infra.entity.InsumoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaInsumoRepository extends JpaRepository<InsumoEntity, UUID> {
  Page<InsumoEntity> findByAtivoTrue(Pageable pageable);

  @Query(
      "SELECT i FROM InsumoEntity i WHERE i.quantidadeEstoque <= i.estoqueMinimo AND i.ativo ="
          + " true")
  List<InsumoEntity> findEstoqueBaixo();

  @Query(
      "SELECT i FROM InsumoEntity i "
          + "WHERE (lower(i.nome) LIKE lower(concat('%', :termo, '%')) "
          + "OR lower(i.descricao) LIKE lower(concat('%', :termo, '%')))"
          + " AND i.ativo = true")
  Page<InsumoEntity> search(@Param("termo") String termo, Pageable pageable);
}
