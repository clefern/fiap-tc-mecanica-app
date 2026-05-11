package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.repository.AtendenteRepository;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.MecanicoRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.infra.jpa.JpaAtendenteRepository;
import com.fiap.mecanica.infra.jpa.JpaClienteRepository;
import com.fiap.mecanica.infra.jpa.JpaMecanicoRepository;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import com.fiap.mecanica.infra.jpa.JpaVeiculoRepository;
import com.fiap.mecanica.infra.seeding.SeedingOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles({"test", "dev-seeding"})
@TestPropertySource(properties = "seeding.enabled=false")
@Transactional
class SeedingIntegrationTest {

  @MockBean private JavaMailSender javaMailSender;

  @Autowired private SeedingOrchestrator seedingOrchestrator;

  @Autowired private ClienteRepository clienteRepository;

  @Autowired private VeiculoRepository veiculoRepository;

  @Autowired private MecanicoRepository mecanicoRepository;

  @Autowired private AtendenteRepository atendenteRepository;

  @Autowired private JpaOrdemServicoRepository jpaOrdemServicoRepository;

  @Autowired private JpaVeiculoRepository jpaVeiculoRepository;
  @Autowired private JpaClienteRepository jpaClienteRepository;
  @Autowired private JpaMecanicoRepository jpaMecanicoRepository;
  @Autowired private JpaAtendenteRepository jpaAtendenteRepository;
  @Autowired private com.fiap.mecanica.infra.jpa.JpaUserRepository jpaUserRepository;

  @Test
  @DisplayName("Should seed all entities correctly")
  void shouldSeedAllEntities() throws Exception {
    // Clean up to force seeding logic to run (Order matters due to FKs)
    jpaOrdemServicoRepository.deleteAll();
    jpaVeiculoRepository.deleteAll();
    jpaClienteRepository.deleteAll();
    jpaMecanicoRepository.deleteAll();
    jpaAtendenteRepository.deleteAll();
    jpaUserRepository.deleteAll();

    // Run the seeder
    seedingOrchestrator.seed();

    // Verify Clientes
    assertThat(clienteRepository.findAll(Pageable.unpaged()).getContent())
        .as("Should have seeded clients")
        .hasSizeGreaterThanOrEqualTo(50);

    // Verify Veiculos - usando a nova assinatura de retorno List
    assertThat(veiculoRepository.findAllByClienteId(java.util.UUID.randomUUID()))
        .as(
            "Should verify vehicles existence differently now that findAll is not paginated in"
                + " repo")
        .isNotNull();
    // A melhor verificação aqui seria via JPA Repository direto pois o Domain
    // Repository
    // mudou para findAllByClienteId que exige um ID específico.
    // Vamos verificar via JPA Repository para garantir contagem total
    assertThat(jpaVeiculoRepository.count())
        .as("Should have seeded vehicles")
        .isGreaterThanOrEqualTo(50);

    // Verify Mecanicos
    assertThat(mecanicoRepository.findAll(Pageable.unpaged()).getContent())
        .as("Should have seeded mechanics")
        .hasSizeGreaterThanOrEqualTo(10);

    // Verify Atendentes
    assertThat(atendenteRepository.findAll(Pageable.unpaged()).getContent())
        .as("Should have seeded attendants")
        .hasSizeGreaterThanOrEqualTo(5);
  }
}
