package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.enums.UserRole;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.infra.adapter.JpaOrdemServicoRepositoryAdapter;
import com.fiap.mecanica.infra.entity.ClienteEntity;
import com.fiap.mecanica.infra.entity.OrdemServicoEntity;
import com.fiap.mecanica.infra.entity.VeiculoEntity;
import com.fiap.mecanica.infra.jpa.JpaClienteRepository;
import com.fiap.mecanica.infra.jpa.JpaOrdemServicoRepository;
import com.fiap.mecanica.infra.jpa.JpaVeiculoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrdemServicoPriorityIntegrationTest {

  @MockitoBean private JavaMailSender javaMailSender;

  @Autowired private JpaOrdemServicoRepositoryAdapter repositoryAdapter;

  @Autowired private JpaOrdemServicoRepository jpaRepository;

  @Autowired private JpaClienteRepository clienteRepository;

  @Autowired private JpaVeiculoRepository veiculoRepository;

  private UUID clienteId;
  private UUID veiculoId;

  @BeforeEach
  void setUp() {
    jpaRepository.deleteAll();
    clienteRepository.deleteAll();
    veiculoRepository.deleteAll();

    // Setup dependencies
    var cliente = new ClienteEntity();
    cliente.setNome("Cliente Teste");
    cliente.setEmail("cliente@teste.com");
    cliente.setPassword("123456"); // Assuming password field exists in UserEntity
    cliente.setDocumento("12345678900");
    cliente.setTipoPessoa(TipoPessoa.FISICA.name()); // Or enum if changed
    cliente.setRole(UserRole.CLIENTE);
    cliente.setTelefone("11999999999");
    cliente.setEndereco("Rua Teste 123");

    cliente = clienteRepository.save(cliente);
    clienteId = cliente.getId();

    var veiculo = new VeiculoEntity();
    veiculo.setPlaca("ABC1234");
    veiculo.setModelo("Modelo Teste");
    veiculo.setMarca("Marca Teste");
    veiculo.setAno(2020);
    veiculo.setCliente(cliente);

    veiculo = veiculoRepository.save(veiculo);
    veiculoId = veiculo.getId();
  }

  @Test
  @DisplayName(
      "Deve listar fila de orçamento ordenada por prioridade (desc) e data de criação (asc)")
  void shouldListFilaOrcamentoOrdered() {
    // Arrange
    createAndSaveOS(StatusOS.RECEBIDA, Prioridade.BAIXA, LocalDateTime.now());
    createAndSaveOS(StatusOS.RECEBIDA, Prioridade.ALTA, LocalDateTime.now());
    createAndSaveOS(StatusOS.RECEBIDA, Prioridade.NORMAL, LocalDateTime.now());

    var osAltaOld =
        createAndSaveOS(StatusOS.RECEBIDA, Prioridade.ALTA, LocalDateTime.now().minusHours(1));
    var osUrgente = createAndSaveOS(StatusOS.RECEBIDA, Prioridade.URGENTE, LocalDateTime.now());

    // Act
    Page<OrdemServico> pageResult = repositoryAdapter.listarFilaOrcamento(Pageable.unpaged());
    List<OrdemServico> result = pageResult.getContent();

    // Assert
    assertThat(result).hasSize(5);
    assertThat(result.getFirst().getPrioridade()).isEqualTo(Prioridade.URGENTE);
    assertThat(result.get(1).getPrioridade()).isEqualTo(Prioridade.ALTA);
    assertThat(result.get(1).getId()).isEqualTo(osAltaOld.getId()); // Older ALTA comes first
    assertThat(result.get(4).getPrioridade()).isEqualTo(Prioridade.BAIXA);
  }

  @Test
  @DisplayName(
      "Deve listar fila de execução ordenada por prioridade (desc) e data de aprovação (asc)")
  void shouldListFilaExecucaoOrdered() {
    // Arrange
    createAndSaveOSWithAprovacao(StatusOS.APROVADA, Prioridade.BAIXA, LocalDateTime.now());
    createAndSaveOSWithAprovacao(StatusOS.APROVADA, Prioridade.ALTA, LocalDateTime.now());

    var osAltaOld =
        createAndSaveOSWithAprovacao(
            StatusOS.APROVADA, Prioridade.ALTA, LocalDateTime.now().minusHours(1));
    var osUrgente =
        createAndSaveOSWithAprovacao(StatusOS.APROVADA, Prioridade.URGENTE, LocalDateTime.now());

    // Act
    Page<OrdemServico> pageResult = repositoryAdapter.listarFilaExecucao(Pageable.unpaged());
    List<OrdemServico> result = pageResult.getContent();

    // Assert
    assertThat(result).hasSize(4);
    assertThat(result.getFirst().getPrioridade()).isEqualTo(Prioridade.URGENTE);
    assertThat(result.get(1).getPrioridade()).isEqualTo(Prioridade.ALTA);
    assertThat(result.get(1).getId()).isEqualTo(osAltaOld.getId()); // Older ALTA comes first
    assertThat(result.get(3).getPrioridade()).isEqualTo(Prioridade.BAIXA);
  }

  private OrdemServicoEntity createAndSaveOS(
      StatusOS status, Prioridade prioridade, LocalDateTime createdAt) {
    var os =
        OrdemServicoEntity.builder()
            .clienteId(clienteId)
            .veiculoId(veiculoId)
            .codigo("OS-" + UUID.randomUUID().toString().substring(0, 8))
            .status(status)
            .prioridade(prioridade)
            .valorTotal(BigDecimal.ZERO)
            .dataEntrada(LocalDateTime.now())
            .createdAt(createdAt)
            .observacoes("Teste de prioridade")
            .build();
    return jpaRepository.save(os);
  }

  private OrdemServicoEntity createAndSaveOSWithAprovacao(
      StatusOS status, Prioridade prioridade, LocalDateTime dataAprovacao) {
    var os =
        OrdemServicoEntity.builder()
            .clienteId(clienteId)
            .veiculoId(veiculoId)
            .codigo("OS-" + UUID.randomUUID().toString().substring(0, 8))
            .status(status)
            .prioridade(prioridade)
            .valorTotal(BigDecimal.TEN)
            .dataEntrada(LocalDateTime.now().minusDays(1))
            .dataAprovacao(dataAprovacao)
            .createdAt(LocalDateTime.now().minusDays(2))
            .observacoes("Teste de execução")
            .build();
    return jpaRepository.save(os);
  }
}
