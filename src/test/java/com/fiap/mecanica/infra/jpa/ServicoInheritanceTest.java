package com.fiap.mecanica.infra.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.infra.entity.ItemComercialEntity;
import com.fiap.mecanica.infra.entity.ServicoEntity;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class ServicoInheritanceTest {

  @Autowired private JpaItemComercialRepository itemComercialRepository;

  @Autowired private JpaServicoRepository servicoRepository;

  @Test
  @DisplayName("Deve persistir e recuperar Servico como ItemComercial")
  void shouldPersistAndRetrieveServicoAsItemComercial() {
    // Arrange
    ServicoEntity servico = new ServicoEntity();
    servico.setNome("Troca de Óleo");
    servico.setDescricao("Troca completa");
    servico.setPrecoBase(new BigDecimal("150.00"));
    servico.setAtivo(true);
    servico.setTempoEstimadoMinutos(45L);
    servico.setCategoria(CategoriaServico.MANUTENCAO_PREVENTIVA);

    // Act - Save via ServicoRepository (which is JpaRepository<ServicoEntity>)
    ServicoEntity saved = servicoRepository.save(servico);

    // Assert - Retrieve via ItemComercialRepository
    Optional<ItemComercialEntity> retrieved = itemComercialRepository.findById(saved.getId());

    assertThat(retrieved).isPresent();
    assertThat(retrieved.get()).isInstanceOf(ServicoEntity.class);

    ServicoEntity retrievedServico = (ServicoEntity) retrieved.get();
    assertThat(retrievedServico.getNome()).isEqualTo("Troca de Óleo");
    assertThat(retrievedServico.getCategoria()).isEqualTo(CategoriaServico.MANUTENCAO_PREVENTIVA);
  }
}
