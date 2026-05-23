package com.fiap.mecanica.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.application.service.OrcamentoService;
import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.StatusOrcamento;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.enums.TipoPessoa;
import com.fiap.mecanica.domain.model.Cliente;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Orcamento;
import com.fiap.mecanica.domain.model.OrdemServico;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.model.Veiculo;
import com.fiap.mecanica.domain.repository.ClienteRepository;
import com.fiap.mecanica.domain.repository.OrcamentoRepository;
import com.fiap.mecanica.domain.repository.OrdemServicoRepository;
import com.fiap.mecanica.domain.repository.PecaRepository;
import com.fiap.mecanica.domain.repository.VeiculoRepository;
import com.fiap.mecanica.domain.valueobject.CPF;
import com.fiap.mecanica.domain.valueobject.Email;
import com.fiap.mecanica.domain.valueobject.Endereco;
import com.fiap.mecanica.domain.valueobject.PlacaVeiculo;
import com.fiap.mecanica.domain.valueobject.TelefoneBr;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrcamentoEstoqueIntegrationTest {

  @Autowired private OrcamentoService orcamentoService;
  @Autowired private OrcamentoRepository orcamentoRepository;
  @Autowired private OrdemServicoRepository ordemServicoRepository;
  @Autowired private PecaRepository pecaRepository;
  @Autowired private ClienteRepository clienteRepository;
  @Autowired private VeiculoRepository veiculoRepository;

  @MockitoBean private JavaMailSender javaMailSender;

  @Test
  @DisplayName("Deve baixar estoque de peças ao aprovar orçamento (Integration)")
  void deveBaixarEstoqueAoAprovarOrcamento() {
    // 1. Setup: Criar Peça com estoque inicial
    Peca peca =
        new Peca(
            null, // ID gerado na persistência se não fornecido? Ou JPA gera.
            // Domain Peca usually has constructor with ID. Let's pass null and see if Adapter
            // handles or if we need to mock UUID.
            // Actually JPA entities usually generate ID. But Domain Peca constructor expects ID.
            // If I pass null, it might be null in domain object.
            // Let's pass null and rely on repository to assign ID upon saving.
            // Wait, Peca constructor:
            // UUID id, String nome, String descricao, BigDecimal precoBase, boolean ativo,
            // String fabricante, String codigoFabricante, String modelo, Integer quantidadeEstoque,
            // Integer estoqueMinimo, Integer estoqueMaximo
            "Filtro de Óleo",
            "Filtro de óleo padrão",
            new BigDecimal("20.00"),
            true,
            "Fabricante X",
            "COD-123",
            "Modelo Y",
            10, // Estoque inicial
            2,
            100);
    // Note: Peca constructor signature from previous read:
    // (UUID id, String nome, String descricao, BigDecimal precoBase, boolean ativo, String
    // fabricante, String codigoFabricante, String modelo, Integer quantidadeEstoque, Integer
    // estoqueMinimo, Integer estoqueMaximo)
    // Wait, I missed the ID in my call above? No, passed null as first arg.
    // Let's check signature again carefully.
    // public Peca(UUID id, String nome, String descricao, BigDecimal precoBase, boolean ativo,
    // String fabricante, String codigoFabricante, String modelo, Integer quantidadeEstoque, Integer
    // estoqueMinimo, Integer estoqueMaximo)

    // Wait, I need to match exactly.
    // 1. id (null)
    // 2. nome ("Filtro de Óleo")
    // 3. descricao ("Filtro de óleo padrão")
    // 4. precoBase (20.00)
    // 5. ativo (true)
    // 6. fabricante ("Fabricante X")
    // 7. codigoFabricante ("COD-123")
    // 8. modelo ("Modelo Y")
    // 9. quantidadeEstoque (10)
    // 10. estoqueMinimo (2)
    // 11. estoqueMaximo (100)

    peca = pecaRepository.save(peca);

    // 2. Setup: Criar Cliente e Veículo (necessários para OS)
    Cliente cliente = criarCliente();
    Veiculo veiculo = criarVeiculo(cliente.getId());

    // 3. Setup: Criar OS com Item referenciando a Peça
    OrdemServico os =
        OrdemServico.builder()
            .codigo("OS-" + UUID.randomUUID().toString().substring(0, 8))
            .clienteId(cliente.getId())
            .veiculoId(veiculo.getId())
            .status(StatusOS.RECEBIDA) // Status inicial
            .prioridade(Prioridade.BAIXA)
            .dataEntrada(LocalDateTime.now())
            .valorTotal(BigDecimal.ZERO)
            .build();
    os = ordemServicoRepository.save(os);

    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .descricao(peca.getNome()) // Usando descricao em vez de nome
            .tipo(TipoItem.PECA)
            .referenciaId(peca.getId())
            .quantidade(2) // Vamos baixar 2 unidades
            .valorUnitario(new BigDecimal("40.00")) // Preço de venda
            .build();

    // Adicionar item e salvar OS novamente
    os.adicionarItem(item);
    os = ordemServicoRepository.save(os);

    // 4. Setup: Criar Orçamento para a OS
    Orcamento orcamento =
        Orcamento.builder()
            .codigo("ORC-" + UUID.randomUUID().toString().substring(0, 8))
            .ordemServicoId(os.getId())
            .dataEmissao(LocalDateTime.now())
            .dataValidade(LocalDateTime.now().plusDays(7))
            .valorTotalMateriais(item.getSubtotal()) // getSubtotal()
            .valorTotalMaoDeObra(BigDecimal.ZERO)
            .valorImpostos(BigDecimal.ZERO)
            .valorTotal(item.getSubtotal())
            .status(StatusOrcamento.GERADO)
            .build();
    orcamento = orcamentoRepository.save(orcamento);

    // 5. Action: Aprovar Orçamento
    // Aqui é onde o método processarBaixaEstoque deve ser chamado
    orcamentoService.aprovar(orcamento.getId());

    // 6. Assertion: Verificar se o estoque foi baixado
    // Precisamos buscar a peça novamente do repositório para ver o estado atual
    Peca pecaAtualizada =
        pecaRepository
            .findById(peca.getId())
            .orElseThrow(() -> new IllegalStateException("Peça não encontrada"));

    // Estoque inicial (10) - Quantidade consumida (2) = 8
    assertThat(pecaAtualizada.getQuantidadeEstoque())
        .as("O estoque da peça deve ser reduzido após aprovação do orçamento")
        .isEqualTo(8);
  }

  private Cliente criarCliente() {
    Cliente cliente =
        new Cliente(
            "João da Silva",
            CPF.of("123.456.789-09"),
            TipoPessoa.FISICA,
            Email.of("joao.silva@email.com"),
            TelefoneBr.of("11999999999"),
            Endereco.of("Rua Teste, 123"));
    return clienteRepository.save(cliente);
  }

  private Veiculo criarVeiculo(UUID clienteId) {
    Veiculo veiculo = new Veiculo(PlacaVeiculo.of("ABC-1234"), "Fusca", "Volkswagen", 1970);
    return veiculoRepository.save(clienteId, veiculo);
  }
}
