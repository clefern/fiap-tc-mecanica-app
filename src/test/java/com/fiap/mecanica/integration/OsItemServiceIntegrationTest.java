package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.fiap.mecanica.application.service.OsItemService;
import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.domain.repository.PecaRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OsItemServiceIntegrationTest {

  @Autowired private OsItemService osItemService;
  @Autowired private OrdemServicoRepository ordemServicoRepository;
  @Autowired private PecaRepository pecaRepository;
  @Autowired private ClienteRepository clienteRepository;
  @Autowired private VeiculoRepository veiculoRepository;

  @PersistenceContext private EntityManager entityManager;

  @MockBean private JavaMailSender javaMailSender;

  @Test
  @DisplayName(
      "Deve adicionar segundo item em OS EM_DIAGNOSTICO sem ObjectOptimisticLockingFailureException")
  void deveAdicionarSegundoItemEmOsEmDiagnosticoSemOptimisticLockingFailure() {
    UUID osId = setupOsEmDiagnosticoComUmItemPersistido();
    UUID pecaId2 = criarPeca("Filtro Ar", 5).getId();

    flushAndClear();

    ItemOrdemServico segundoItem =
        ItemOrdemServico.builder()
            .descricao("Filtro Ar")
            .tipo(TipoItem.PECA)
            .referenciaId(pecaId2)
            .quantidade(1)
            .valorUnitario(new BigDecimal("80.00"))
            .build();

    assertThatNoException().isThrownBy(() -> osItemService.adicionarItem(osId, segundoItem, null));

    flushAndClear();

    OrdemServico recarregada =
        ordemServicoRepository
            .findByIdWithItens(osId)
            .orElseThrow(() -> new IllegalStateException("OS desapareceu"));
    assertThat(recarregada.getItens())
        .as("Deve persistir os 2 itens com IDs distintos")
        .hasSize(2);
    assertThat(recarregada.getItens().stream().map(ItemOrdemServico::getId).distinct().count())
        .isEqualTo(2);
    assertThat(recarregada.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
  }

  @Test
  @DisplayName("Deve atualizar quantidade de item existente sem recriar entidade")
  void deveAtualizarQuantidadeDeItemExistente() {
    UUID osId = setupOsEmDiagnosticoComUmItemPersistido();
    flushAndClear();

    OrdemServico carregada =
        ordemServicoRepository
            .findByIdWithItens(osId)
            .orElseThrow(() -> new IllegalStateException("OS não encontrada"));
    UUID itemId = carregada.getItens().get(0).getId();

    osItemService.atualizarQuantidadeItem(osId, itemId, 5, null);
    flushAndClear();

    OrdemServico recarregada =
        ordemServicoRepository
            .findByIdWithItens(osId)
            .orElseThrow(() -> new IllegalStateException("OS desapareceu"));
    assertThat(recarregada.getItens()).hasSize(1);
    assertThat(recarregada.getItens().get(0).getId()).isEqualTo(itemId);
    assertThat(recarregada.getItens().get(0).getQuantidade()).isEqualTo(5);
  }

  @Test
  @DisplayName("Deve remover item via reconciliação (orphanRemoval) sem exception")
  void deveRemoverItemViaReconciliacao() {
    UUID osId = setupOsEmDiagnosticoComUmItemPersistido();
    flushAndClear();

    OrdemServico carregada =
        ordemServicoRepository
            .findByIdWithItens(osId)
            .orElseThrow(() -> new IllegalStateException("OS não encontrada"));
    UUID itemId = carregada.getItens().get(0).getId();

    osItemService.removerItem(osId, itemId, null);
    flushAndClear();

    OrdemServico recarregada =
        ordemServicoRepository
            .findByIdWithItens(osId)
            .orElseThrow(() -> new IllegalStateException("OS desapareceu"));
    assertThat(recarregada.getItens()).isEmpty();
  }

  private UUID setupOsEmDiagnosticoComUmItemPersistido() {
    Cliente cliente = criarCliente();
    Veiculo veiculo = criarVeiculo(cliente.getId());
    Peca peca1 = criarPeca("Filtro Óleo", 10);

    OrdemServico os =
        OrdemServico.builder()
            .codigo("OS-" + UUID.randomUUID().toString().substring(0, 8))
            .clienteId(cliente.getId())
            .veiculoId(veiculo.getId())
            .status(StatusOS.RECEBIDA)
            .prioridade(Prioridade.BAIXA)
            .dataEntrada(LocalDateTime.now())
            .valorTotal(BigDecimal.ZERO)
            .build();
    os = ordemServicoRepository.save(os);

    ItemOrdemServico primeiroItem =
        ItemOrdemServico.builder()
            .descricao("Filtro Óleo")
            .tipo(TipoItem.PECA)
            .referenciaId(peca1.getId())
            .quantidade(1)
            .valorUnitario(new BigDecimal("40.00"))
            .build();

    flushAndClear();
    osItemService.adicionarItem(os.getId(), primeiroItem, null);
    flushAndClear();

    OrdemServico recarregada =
        ordemServicoRepository
            .findByIdWithItens(os.getId())
            .orElseThrow(() -> new IllegalStateException("OS sumiu"));
    recarregada.iniciarDiagnostico();
    ordemServicoRepository.save(recarregada);
    flushAndClear();

    return os.getId();
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private Cliente criarCliente() {
    String cpf = randomCpf();
    Cliente cliente =
        new Cliente(
            "Cliente Teste",
            CPF.of(cpf),
            TipoPessoa.FISICA,
            Email.of("teste-" + UUID.randomUUID() + "@email.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua Teste, 123"));
    return clienteRepository.save(cliente);
  }

  private Veiculo criarVeiculo(UUID clienteId) {
    String placa = randomPlaca();
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of(placa), "Fusca", "Volkswagen", 1970);
    return veiculoRepository.save(clienteId, veiculo);
  }

  private Peca criarPeca(String nome, int estoque) {
    Peca peca =
        new Peca(
            null,
            nome,
            nome + " desc",
            new BigDecimal("20.00"),
            true,
            "Fab",
            "COD-" + UUID.randomUUID().toString().substring(0, 6),
            "Modelo",
            estoque,
            1,
            100);
    return pecaRepository.save(peca);
  }

  private static String randomCpf() {
    int[] n = new int[9];
    for (int i = 0; i < 9; i++) n[i] = (int) (Math.random() * 10);
    int d1 = calcDigit(n, 10);
    int d2 = calcDigit(append(n, d1), 11);
    return String.format(
        "%d%d%d.%d%d%d.%d%d%d-%d%d",
        n[0], n[1], n[2], n[3], n[4], n[5], n[6], n[7], n[8], d1, d2);
  }

  private static int[] append(int[] arr, int v) {
    int[] r = new int[arr.length + 1];
    System.arraycopy(arr, 0, r, 0, arr.length);
    r[arr.length] = v;
    return r;
  }

  private static int calcDigit(int[] digits, int startWeight) {
    int sum = 0;
    int weight = startWeight;
    for (int d : digits) {
      sum += d * weight;
      weight--;
    }
    int mod = sum % 11;
    return mod < 2 ? 0 : 11 - mod;
  }

  private static String randomPlaca() {
    String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 3; i++) sb.append(letters.charAt((int) (Math.random() * 26)));
    sb.append("-");
    for (int i = 0; i < 4; i++) sb.append((int) (Math.random() * 10));
    return sb.toString();
  }
}
