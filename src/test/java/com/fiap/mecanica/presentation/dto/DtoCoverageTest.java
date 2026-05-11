package com.fiap.mecanica.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiap.mecanica.domain.enums.CategoriaServico;
import com.fiap.mecanica.domain.enums.Prioridade;
import com.fiap.mecanica.domain.enums.StatusEstoque;
import com.fiap.mecanica.domain.enums.StatusOS;
import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.presentation.dto.auth.ForgotPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.ResetPasswordRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenRequest;
import com.fiap.mecanica.presentation.dto.auth.TokenResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DtoCoverageTest {

  @Test
  @DisplayName("Deve cobrir DTOs de Autenticação")
  void shouldCoverAuthDtos() {
    TokenRequest tokenRequest =
        new TokenRequest("password", "user", "123456", null, "client", "secret", null);

    assertThat(tokenRequest.grantType()).isEqualTo("password");
    assertThat(tokenRequest.username()).isEqualTo("user");
    assertThat(tokenRequest.password()).isEqualTo("123456");

    TokenResponse tokenResponse = new TokenResponse("access", "refresh", "Bearer", 3600L);

    assertThat(tokenResponse.accessToken()).isEqualTo("access");
    assertThat(tokenResponse.refreshToken()).isEqualTo("refresh");
    assertThat(tokenResponse.tokenType()).isEqualTo("Bearer");
    assertThat(tokenResponse.expiresIn()).isEqualTo(3600L);

    ForgotPasswordRequest forgotPasswordRequest =
        ForgotPasswordRequest.builder().email("test@test.com").build();
    assertThat(forgotPasswordRequest.getEmail()).isEqualTo("test@test.com");

    ResetPasswordRequest resetPasswordRequest =
        ResetPasswordRequest.builder()
            .token("token")
            .email("user@test.com")
            .newPassword("newPass123")
            .build();

    assertThat(resetPasswordRequest.getToken()).isEqualTo("token");
    assertThat(resetPasswordRequest.getEmail()).isEqualTo("user@test.com");
    assertThat(resetPasswordRequest.getNewPassword()).isEqualTo("newPass123");
  }

  @Test
  @DisplayName("Deve cobrir DTOs de Estoque")
  void shouldCoverEstoqueDtos() {
    UUID referenciaId = UUID.randomUUID();

    EntradaEstoqueRequest entrada = new EntradaEstoqueRequest();
    entrada.setReferenciaId(referenciaId);
    entrada.setTipo(TipoItem.PECA);
    entrada.setQuantidade(10);

    assertThat(entrada.getReferenciaId()).isEqualTo(referenciaId);
    assertThat(entrada.getTipo()).isEqualTo(TipoItem.PECA);
    assertThat(entrada.getQuantidade()).isEqualTo(10);

    BaixaEstoqueRequest baixa = new BaixaEstoqueRequest();
    baixa.setReferenciaId(referenciaId);
    baixa.setTipo(TipoItem.INSUMO);
    baixa.setQuantidade(5);

    assertThat(baixa.getReferenciaId()).isEqualTo(referenciaId);
    assertThat(baixa.getTipo()).isEqualTo(TipoItem.INSUMO);
    assertThat(baixa.getQuantidade()).isEqualTo(5);

    AtualizarParametrosEstoqueRequest parametros = new AtualizarParametrosEstoqueRequest();
    parametros.setReferenciaId(referenciaId);
    parametros.setTipo(TipoItem.PECA);
    parametros.setEstoqueMinimo(2);
    parametros.setEstoqueMaximo(20);

    assertThat(parametros.getReferenciaId()).isEqualTo(referenciaId);
    assertThat(parametros.getTipo()).isEqualTo(TipoItem.PECA);
    assertThat(parametros.getEstoqueMinimo()).isEqualTo(2);
    assertThat(parametros.getEstoqueMaximo()).isEqualTo(20);
  }

  @Test
  @DisplayName("Deve cobrir DTOs de Itens")
  void shouldCoverItemDtos() {
    PecaRequest pecaRequest = new PecaRequest();
    pecaRequest.setNome("Pastilha");
    pecaRequest.setDescricao("Pastilha dianteira");
    pecaRequest.setPrecoBase(BigDecimal.valueOf(100));
    pecaRequest.setFabricante("Fabricante");
    pecaRequest.setCodigoFabricante("CODE-1");
    pecaRequest.setModelo("Modelo");
    pecaRequest.setAtivo(true);
    pecaRequest.setQuantidadeEstoque(50);
    pecaRequest.setEstoqueMinimo(5);
    pecaRequest.setEstoqueMaximo(100);

    assertThat(pecaRequest.getNome()).isEqualTo("Pastilha");
    assertThat(pecaRequest.getDescricao()).isEqualTo("Pastilha dianteira");
    assertThat(pecaRequest.getPrecoBase()).isEqualTo(BigDecimal.valueOf(100));
    assertThat(pecaRequest.getFabricante()).isEqualTo("Fabricante");
    assertThat(pecaRequest.getCodigoFabricante()).isEqualTo("CODE-1");
    assertThat(pecaRequest.getModelo()).isEqualTo("Modelo");
    assertThat(pecaRequest.isAtivo()).isTrue();

    PecaResponse pecaResponse = new PecaResponse();
    UUID pecaId = UUID.randomUUID();
    pecaResponse.setId(pecaId);
    pecaResponse.setNome("Pastilha");
    pecaResponse.setDescricao("Pastilha dianteira");
    pecaResponse.setPrecoBase(BigDecimal.valueOf(100));
    pecaResponse.setFabricante("Fabricante");
    pecaResponse.setCodigoFabricante("CODE-1");
    pecaResponse.setModelo("Modelo");
    pecaResponse.setAtivo(true);
    pecaResponse.setQuantidadeEstoque(50);
    pecaResponse.setEstoqueMinimo(5);
    pecaResponse.setEstoqueMaximo(100);
    pecaResponse.setStatusEstoque(StatusEstoque.NORMAL);

    assertThat(pecaResponse.getId()).isEqualTo(pecaId);
    assertThat(pecaResponse.getStatusEstoque()).isEqualTo(StatusEstoque.NORMAL);

    InsumoRequest insumoRequest = new InsumoRequest();
    insumoRequest.setNome("Óleo");
    insumoRequest.setDescricao("Óleo sintético");
    insumoRequest.setPrecoBase(BigDecimal.valueOf(50));
    insumoRequest.setUnidadeMedida("LITRO");
    insumoRequest.setAtivo(true);
    insumoRequest.setQuantidadeEstoque(100);
    insumoRequest.setEstoqueMinimo(10);
    insumoRequest.setEstoqueMaximo(200);

    assertThat(insumoRequest.getNome()).isEqualTo("Óleo");
    assertThat(insumoRequest.getUnidadeMedida()).isEqualTo("LITRO");

    InsumoResponse insumoResponse = new InsumoResponse();
    UUID insumoId = UUID.randomUUID();
    insumoResponse.setId(insumoId);
    insumoResponse.setNome("Óleo");
    insumoResponse.setDescricao("Óleo sintético");
    insumoResponse.setPrecoBase(BigDecimal.valueOf(50));
    insumoResponse.setUnidadeMedida("LITRO");
    insumoResponse.setAtivo(true);
    insumoResponse.setQuantidadeEstoque(100);
    insumoResponse.setEstoqueMinimo(10);
    insumoResponse.setEstoqueMaximo(200);
    insumoResponse.setStatusEstoque(StatusEstoque.PRE_ALERTA);

    assertThat(insumoResponse.getId()).isEqualTo(insumoId);
    assertThat(insumoResponse.getStatusEstoque()).isEqualTo(StatusEstoque.PRE_ALERTA);
  }

  @Test
  @DisplayName("Deve cobrir DTOs de Serviço e OS")
  void shouldCoverServicoOsDtos() {
    ServicoRequest servicoRequest = new ServicoRequest();
    servicoRequest.setNome("Troca de óleo");
    servicoRequest.setDescricao("Troca de óleo completa");
    servicoRequest.setValorBase(BigDecimal.valueOf(150));
    servicoRequest.setTempoEstimadoMinutos(45L);
    servicoRequest.setCategoria(CategoriaServico.MANUTENCAO_PREVENTIVA);
    servicoRequest.setAtivo(true);

    assertThat(servicoRequest.getNome()).isEqualTo("Troca de óleo");
    assertThat(servicoRequest.getCategoria()).isEqualTo(CategoriaServico.MANUTENCAO_PREVENTIVA);

    ServicoResponse servicoResponse = new ServicoResponse();
    UUID servicoId = UUID.randomUUID();
    servicoResponse.setId(servicoId);
    servicoResponse.setNome("Troca de óleo");
    servicoResponse.setDescricao("Troca de óleo completa");
    servicoResponse.setValorBase(BigDecimal.valueOf(150));
    servicoResponse.setTempoEstimadoMinutos(45L);
    servicoResponse.setCategoria(CategoriaServico.MANUTENCAO_PREVENTIVA);
    servicoResponse.setAtivo(true);

    assertThat(servicoResponse.getId()).isEqualTo(servicoId);

    AdicionarItemRequest adicionarItemRequest = new AdicionarItemRequest();
    UUID refId = UUID.randomUUID();
    adicionarItemRequest.setTipo(TipoItem.SERVICO);
    adicionarItemRequest.setDescricao("Alinhamento");
    adicionarItemRequest.setValorUnitario(BigDecimal.valueOf(80));
    adicionarItemRequest.setQuantidade(1);
    adicionarItemRequest.setReferenciaId(refId);

    assertThat(adicionarItemRequest.getTipo()).isEqualTo(TipoItem.SERVICO);
    assertThat(adicionarItemRequest.getReferenciaId()).isEqualTo(refId);

    TrocarMecanicoRequest trocarMecanicoRequest = new TrocarMecanicoRequest(UUID.randomUUID());
    assertThat(trocarMecanicoRequest.novoMecanicoId()).isNotNull();

    AtualizarQuantidadeItemRequest atualizarQuantidadeItemRequest =
        new AtualizarQuantidadeItemRequest();
    atualizarQuantidadeItemRequest.setQuantidade(3);
    assertThat(atualizarQuantidadeItemRequest.getQuantidade()).isEqualTo(3);

    OrdemServicoResponse osResponse = new OrdemServicoResponse();
    UUID osId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();
    UUID veiculoId = UUID.randomUUID();
    osResponse.setId(osId);
    osResponse.setClienteId(clienteId);
    osResponse.setVeiculoId(veiculoId);
    osResponse.setCodigo("OS123");
    osResponse.setStatus(StatusOS.RECEBIDA);
    osResponse.setDataEntrada(LocalDateTime.now());
    osResponse.setPrioridade(Prioridade.ALTA);

    assertThat(osResponse.getId()).isEqualTo(osId);
    assertThat(osResponse.getStatus()).isEqualTo(StatusOS.RECEBIDA);
    assertThat(osResponse.getPrioridade()).isEqualTo(Prioridade.ALTA);

    ItemOrdemServicoResponse itemResponse = new ItemOrdemServicoResponse();
    UUID itemId = UUID.randomUUID();
    itemResponse.setId(itemId);
    itemResponse.setTipo(TipoItem.PECA);
    itemResponse.setDescricao("Pastilha");
    itemResponse.setValorUnitario(BigDecimal.valueOf(100));
    itemResponse.setQuantidade(2);
    itemResponse.setSubtotal(BigDecimal.valueOf(200));
    itemResponse.setReferenciaId(refId);

    assertThat(itemResponse.getId()).isEqualTo(itemId);
    assertThat(itemResponse.getTipo()).isEqualTo(TipoItem.PECA);
    assertThat(itemResponse.getSubtotal()).isEqualTo(BigDecimal.valueOf(200));

    osResponse.setItens(List.of(itemResponse));
    assertThat(osResponse.getItens()).hasSize(1);
  }
}
