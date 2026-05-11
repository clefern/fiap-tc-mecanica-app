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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FilaOperacionalIntegrationTest {

  @MockBean private JavaMailSender javaMailSender;

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

    var cliente = new ClienteEntity();
    cliente.setNome("Cliente Fila Teste");
    cliente.setEmail("fila@teste.com");
    cliente.setPassword("123456");
    cliente.setDocumento("98765432100");
    cliente.setTipoPessoa(TipoPessoa.FISICA.name());
    cliente.setRole(UserRole.CLIENTE);
    cliente.setTelefone("11988888888");
    cliente.setEndereco("Rua Fila 456");

    cliente = clienteRepository.save(cliente);
    clienteId = cliente.getId();

    var veiculo = new VeiculoEntity();
    veiculo.setPlaca("XYZ9999");
    veiculo.setModelo("Modelo Fila");
    veiculo.setMarca("Marca Fila");
    veiculo.setAno(2022);
    veiculo.setCliente(cliente);

    veiculo = veiculoRepository.save(veiculo);
    veiculoId = veiculo.getId();
  }

  @Test
  @DisplayName("Deve excluir OS com status FINALIZADA, ENTREGUE e CANCELADA da fila operacional")
  void deveExcluirStatusFinaisECanceladaDaFilaOperacional() {
    createOS(StatusOS.RECEBIDA);
    createOS(StatusOS.EM_DIAGNOSTICO);
    createOS(StatusOS.FINALIZADA);
    createOS(StatusOS.ENTREGUE);
    createOS(StatusOS.CANCELADA);

    Page<OrdemServico> result = repositoryAdapter.listarFilaOperacional(Pageable.unpaged());

    assertThat(result.getContent()).hasSize(2);
    result
        .getContent()
        .forEach(
            os ->
                assertThat(os.getStatus())
                    .isNotIn(StatusOS.FINALIZADA, StatusOS.ENTREGUE, StatusOS.CANCELADA));
  }

  @Test
  @DisplayName("Deve ordenar EM_EXECUCAO antes de RECEBIDA na fila operacional")
  void deveOrdenarEmExecucaoAntesDeRecebida() {
    createOS(StatusOS.RECEBIDA);
    createOS(StatusOS.EM_EXECUCAO);

    Page<OrdemServico> result = repositoryAdapter.listarFilaOperacional(Pageable.unpaged());
    List<OrdemServico> content = result.getContent();

    assertThat(content).hasSize(2);
    assertThat(content.getFirst().getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
    assertThat(content.get(1).getStatus()).isEqualTo(StatusOS.RECEBIDA);
  }

  @Test
  @DisplayName("Deve respeitar paginação na fila operacional")
  void deveRespeitarPaginacaoNaFilaOperacional() {
    createOS(StatusOS.RECEBIDA);
    createOS(StatusOS.EM_DIAGNOSTICO);
    createOS(StatusOS.APROVADA);

    Page<OrdemServico> page = repositoryAdapter.listarFilaOperacional(PageRequest.of(0, 2));

    assertThat(page.getTotalElements()).isEqualTo(3);
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  private OrdemServicoEntity createOS(StatusOS status) {
    var os =
        OrdemServicoEntity.builder()
            .clienteId(clienteId)
            .veiculoId(veiculoId)
            .codigo("OS-" + UUID.randomUUID().toString().substring(0, 8))
            .status(status)
            .prioridade(Prioridade.NORMAL)
            .valorTotal(BigDecimal.ZERO)
            .dataEntrada(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .observacoes("Teste fila operacional")
            .build();
    return jpaRepository.save(os);
  }
}
