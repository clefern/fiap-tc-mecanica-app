package com.fiap.mecanica.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExceptionCoverageTest {

  @Test
  @DisplayName("Deve cobrir BusinessException")
  void shouldCoverBusinessException() {
    BusinessException ex = new BusinessException("Erro de negócio", "BUS-001") {};
    assertThat(ex).isInstanceOf(MecanicaError.class);
    assertThat(ex.getMessage()).isEqualTo("Erro de negócio");
    assertThat(ex.getCode()).isEqualTo("BUS-001");
  }

  @Test
  @DisplayName("Deve cobrir SystemException")
  void shouldCoverSystemException() {
    SystemException ex = new SystemException("Erro de sistema", "SYS-001") {};
    assertThat(ex).isInstanceOf(MecanicaError.class);
    assertThat(ex.getMessage()).isEqualTo("Erro de sistema");
    assertThat(ex.getCode()).isEqualTo("SYS-001");

    RuntimeException cause = new RuntimeException("cause");
    SystemException exWithCause = new SystemException("Erro de sistema", "SYS-002", cause) {};
    assertThat(exWithCause.getCause()).isEqualTo(cause);
    assertThat(exWithCause.getCode()).isEqualTo("SYS-002");
  }

  @Test
  @DisplayName("Deve cobrir MecanicaException")
  void shouldCoverMecanicaException() {
    MecanicaException ex = new MecanicaException("Erro genérico", "ERR-001") {};
    assertThat(ex).isInstanceOf(MecanicaError.class);
    assertThat(ex.getMessage()).isEqualTo("Erro genérico");
    assertThat(ex.getCode()).isEqualTo("ERR-001");

    RuntimeException cause = new RuntimeException("cause");
    MecanicaException exWithCause = new MecanicaException("Erro genérico", "ERR-002", cause) {};
    assertThat(exWithCause.getCause()).isEqualTo(cause);
    assertThat(exWithCause.getCode()).isEqualTo("ERR-002");
  }

  @Test
  @DisplayName("Deve cobrir DomainRuleException")
  void shouldCoverDomainRuleException() {
    DomainRuleException ex = new DomainRuleException("Regra violada", "DOM-001") {};
    assertThat(ex).isInstanceOf(MecanicaError.class);
    assertThat(ex.getMessage()).isEqualTo("Regra violada");
    assertThat(ex.getCode()).isEqualTo("DOM-001");
  }

  @Test
  @DisplayName("Deve cobrir ResourceNotFoundException")
  void shouldCoverResourceNotFoundException() {
    ResourceNotFoundException ex =
        new ResourceNotFoundException("Recurso não encontrado", "404") {};
    assertThat(ex).isInstanceOf(MecanicaError.class);
    assertThat(ex.getMessage()).isEqualTo("Recurso não encontrado");
    assertThat(ex.getCode()).isEqualTo("404");
  }

  @Test
  @DisplayName("Deve cobrir exceptions específicas de Não Encontrado")
  void shouldCoverNotFoundExceptions() {
    UUID id = UUID.randomUUID();

    ClienteNaoEncontradoException clienteEx = new ClienteNaoEncontradoException(id);
    assertThat(clienteEx.getMessage()).contains(id.toString());

    MecanicoNaoEncontradoException mecanicoEx = new MecanicoNaoEncontradoException(id);
    assertThat(mecanicoEx.getMessage()).contains(id.toString());

    AtendenteNaoEncontradoException atendenteEx = new AtendenteNaoEncontradoException(id);
    assertThat(atendenteEx.getMessage()).contains(id.toString());

    VeiculoNaoEncontradoException veiculoEx = new VeiculoNaoEncontradoException(id);
    assertThat(veiculoEx.getMessage()).contains(id.toString());

    OrdemServicoNaoEncontradaException osEx = new OrdemServicoNaoEncontradaException(id);
    assertThat(osEx.getMessage()).contains(id.toString());

    ServicoNaoEncontradoException servicoEx = new ServicoNaoEncontradoException(id);
    assertThat(servicoEx.getMessage()).contains(id.toString());
  }

  @Test
  @DisplayName("Deve cobrir exceptions de Duplicidade")
  void shouldCoverDuplicateExceptions() {
    DocumentoJaCadastradoException docEx = new DocumentoJaCadastradoException("123");
    assertThat(docEx.getMessage()).contains("123");

    DuplicateDocumentoException dupDocEx = new DuplicateDocumentoException("456");
    assertThat(dupDocEx.getMessage()).contains("456");

    DuplicatePlacaException placaEx = new DuplicatePlacaException("ABC-1234");
    assertThat(placaEx.getMessage()).contains("ABC-1234");

    UUID id = UUID.randomUUID();
    ItemDuplicadoException itemEx = new ItemDuplicadoException("Item já existe", id);
    assertThat(itemEx.getMessage()).contains("Item já existe");
  }

  @Test
  @DisplayName("Deve cobrir outras exceptions de negócio")
  void shouldCoverOtherBusinessExceptions() {
    EstoqueInsuficienteException estoqueEx = new EstoqueInsuficienteException("Sem estoque", 10, 5);
    assertThat(estoqueEx.getMessage()).contains("Sem estoque");

    TransicaoStatusInvalidaException transicaoEx =
        new TransicaoStatusInvalidaException("Status inválido");
    assertThat(transicaoEx.getMessage()).isEqualTo("Status inválido");

    ViolacaoPrioridadeException prioridadeEx =
        new ViolacaoPrioridadeException("Prioridade violada");
    assertThat(prioridadeEx.getMessage()).isEqualTo("Prioridade violada");
  }
}
