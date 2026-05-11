package com.fiap.mecanica.application.service.os;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.domain.enums.TipoItem;
import com.fiap.mecanica.domain.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.domain.model.Insumo;
import com.fiap.mecanica.domain.model.ItemOrdemServico;
import com.fiap.mecanica.domain.model.Peca;
import com.fiap.mecanica.domain.repository.InsumoRepository;
import com.fiap.mecanica.domain.repository.PecaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OsEstoqueValidatorTest {

  @Mock private PecaRepository pecaRepository;
  @Mock private InsumoRepository insumoRepository;

  @InjectMocks private OsEstoqueValidator validator;

  private UUID pecaId;
  private UUID insumoId;

  @BeforeEach
  void setUp() {
    pecaId = UUID.randomUUID();
    insumoId = UUID.randomUUID();
  }

  @Test
  @DisplayName("Deve validar Peca com estoque suficiente sem lançar exceção")
  void devePecaEstoqueSuficienteOk() {
    Peca peca = mock(Peca.class);
    when(peca.getQuantidadeEstoque()).thenReturn(10);
    when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));

    ItemOrdemServico item =
        ItemOrdemServico.builder().tipo(TipoItem.PECA).referenciaId(pecaId).quantidade(5).build();

    validator.validar(item);

    verify(pecaRepository).findById(pecaId);
    verifyNoInteractions(insumoRepository);
  }

  @Test
  @DisplayName("Deve lançar EstoqueInsuficienteException quando Peca sem estoque")
  void deveLancarExcecaoQuandoPecaEstoqueInsuficiente() {
    Peca peca = mock(Peca.class);
    when(peca.getNome()).thenReturn("Filtro de ar");
    when(peca.getQuantidadeEstoque()).thenReturn(3);
    when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));

    ItemOrdemServico item =
        ItemOrdemServico.builder().tipo(TipoItem.PECA).referenciaId(pecaId).quantidade(10).build();

    assertThatThrownBy(() -> validator.validar(item))
        .isInstanceOf(EstoqueInsuficienteException.class)
        .hasMessageContaining("Filtro de ar");
  }

  @Test
  @DisplayName("Deve lançar IllegalArgumentException quando Peca não encontrada")
  void deveLancarExcecaoQuandoPecaNaoEncontrada() {
    when(pecaRepository.findById(pecaId)).thenReturn(Optional.empty());

    ItemOrdemServico item =
        ItemOrdemServico.builder().tipo(TipoItem.PECA).referenciaId(pecaId).quantidade(1).build();

    assertThatThrownBy(() -> validator.validar(item))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Peça não encontrada");
  }

  @Test
  @DisplayName("Deve validar Insumo com estoque suficiente sem lançar exceção")
  void deveInsumoEstoqueSuficienteOk() {
    Insumo insumo = mock(Insumo.class);
    when(insumo.getQuantidadeEstoque()).thenReturn(100);
    when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));

    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .tipo(TipoItem.INSUMO)
            .referenciaId(insumoId)
            .quantidade(10)
            .build();

    validator.validar(item);

    verify(insumoRepository).findById(insumoId);
    verifyNoInteractions(pecaRepository);
  }

  @Test
  @DisplayName("Deve lançar EstoqueInsuficienteException quando Insumo sem estoque")
  void deveLancarExcecaoQuandoInsumoEstoqueInsuficiente() {
    Insumo insumo = mock(Insumo.class);
    when(insumo.getNome()).thenReturn("Óleo sintético");
    when(insumo.getQuantidadeEstoque()).thenReturn(2);
    when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));

    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .tipo(TipoItem.INSUMO)
            .referenciaId(insumoId)
            .quantidade(10)
            .build();

    assertThatThrownBy(() -> validator.validar(item))
        .isInstanceOf(EstoqueInsuficienteException.class)
        .hasMessageContaining("Óleo sintético");
  }

  @Test
  @DisplayName("Deve lançar IllegalArgumentException quando Insumo não encontrado")
  void deveLancarExcecaoQuandoInsumoNaoEncontrado() {
    when(insumoRepository.findById(insumoId)).thenReturn(Optional.empty());

    ItemOrdemServico item =
        ItemOrdemServico.builder()
            .tipo(TipoItem.INSUMO)
            .referenciaId(insumoId)
            .quantidade(1)
            .build();

    assertThatThrownBy(() -> validator.validar(item))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Insumo não encontrado");
  }

  @Test
  @DisplayName("Deve ignorar validação para item do tipo SERVICO")
  void deveIgnorarValidacaoParaServico() {
    ItemOrdemServico item = ItemOrdemServico.builder().tipo(TipoItem.SERVICO).quantidade(1).build();

    validator.validar(item);

    verifyNoInteractions(pecaRepository);
    verifyNoInteractions(insumoRepository);
  }
}
